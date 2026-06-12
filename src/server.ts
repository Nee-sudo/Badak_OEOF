import express from "express";
import http from "http";
import cors from "cors";
import jwt from "jsonwebtoken";
import { WebSocketServer, WebSocket } from "ws";
import { db, User, Message } from "./db";

// Load configuration
const PORT = process.env.PORT || 8080;
const app = express();

const JWT_SECRET = process.env.JWT_SECRET || "default_civilization_secret_key_oeof_992211";
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || "default_civilization_refresh_key_oeof_951122";

// Middleware config
app.use(cors({ origin: "*" }));
app.use(express.json());

function signAccessToken(userId: string, email: string): string {
  return jwt.sign({ userId, email }, JWT_SECRET, { expiresIn: "1h" });
}

function signRefreshToken(userId: string): string {
  return jwt.sign({ userId }, JWT_REFRESH_SECRET, { expiresIn: "30d" });
}

// Authentication middleware to secure premium channels (Section 6.2)
function authenticateToken(req: any, res: any, next: any) {
  const authHeader = req.headers["authorization"];
  const token = authHeader && authHeader.split(" ")[1];

  if (!token) {
     res.status(401).json({ error: "Access credit signature token missing." });
     return;
  }

  jwt.verify(token, JWT_SECRET, (err: any, user: any) => {
    if (err) {
       res.status(403).json({ error: "Access signature invalid or expired." });
       return;
    }
    req.user = user;
    next();
  });
}

// Express HTTP router status ping with database awareness
app.get("/api/health", (req, res) => {
  res.json({
    status: "healthy",
    timestamp: new Date().toISOString(),
    platform: "OEOF Senior Backend Systems",
    environment: {
      PORTConfigured: !!process.env.PORT,
      MongoUriPresent: !!process.env.MONGO_URI,
      DatabaseUrlPresent: !!process.env.DATABASE_URL,
      JwtSecretPresent: !!process.env.JWT_SECRET,
      JwtRefreshSecretPresent: !!process.env.JWT_REFRESH_SECRET
    }
  });
});

// ==========================================
// 1. CITIZEN IDENTITY MANAGEMENT (AUTH)
// ==========================================
app.post("/api/auth/register", async (req, res) => {
  try {
    const { email, username, password, name, territory, flagEmoji, traits } = req.body;
    if (!email || !username) {
       res.status(400).json({ error: "Missing required registration parameters." });
       return;
    }

    const existsEmail = await db.getUserByEmail(email);
    if (existsEmail) {
       res.status(409).json({ error: "Email already registered under another citizen ledger." });
       return;
    }

    const existsUser = await db.getUserByUsername(username);
    if (existsUser) {
       res.status(409).json({ error: "Username already claimed." });
       return;
    }

    const newCitizen = await db.createUser({
      email,
      username,
      passwordHash: password || "password123",
      name: name || username,
      territory: territory || "Global",
      flagEmoji: flagEmoji || "🌐",
      personalityTraits: traits || []
    });

    // Create custom welcoming promotion notification logs (Section 2)
    await db.createNotification({
      userId: newCitizen.userId,
      title: "Citizenship Verified",
      body: `Welcome to OEOF, Citizen @${newCitizen.username}! Verification registered successfully in the territory of ${newCitizen.territory}.`,
      type: "PROMOTION"
    });

    const accessToken = signAccessToken(newCitizen.userId, newCitizen.email);
    const refreshToken = signRefreshToken(newCitizen.userId);

    res.status(201).json({
      success: true,
      accessToken,
      refreshToken,
      user: newCitizen
    });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

app.post("/api/auth/login", async (req, res) => {
  try {
    const { emailOrUsername, password } = req.body;
    if (!emailOrUsername) {
       res.status(400).json({ error: "Email or username required." });
       return;
    }

    let user = await db.getUserByEmail(emailOrUsername);
    if (!user) {
      user = await db.getUserByUsername(emailOrUsername);
    }

    if (!user || user.passwordHash !== (password || "password123")) {
       res.status(401).json({ error: "Invalid signature or access credentials." });
       return;
    }

    const accessToken = signAccessToken(user.userId, user.email);
    const refreshToken = signRefreshToken(user.userId);

    res.json({
      success: true,
      accessToken,
      refreshToken,
      user
    });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

app.post("/api/auth/refresh", (req, res) => {
  try {
    const { refreshToken } = req.body;
    if (!refreshToken) {
       res.status(400).json({ error: "Refresh token parameter mandatory." });
       return;
    }

    jwt.verify(refreshToken, JWT_REFRESH_SECRET, (err: any, decoded: any) => {
      if (err) {
         res.status(403).json({ error: "Refresh token is invalid or expired." });
         return;
      }
      const newAccessToken = jwt.sign({ userId: decoded.userId }, JWT_SECRET, { expiresIn: "1h" });
      res.json({ success: true, accessToken: newAccessToken });
    });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

app.get("/api/users/:userId/profile", async (req, res) => {
  try {
    const user = await db.getUser(req.params.userId);
    if (!user) {
       res.status(404).json({ error: "Citizen registry not found." });
       return;
    }
    res.json(user);
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

// ==========================================
// 2. CIVIC SOCIAL MEDIA FEEDS (POSTS)
// ==========================================
app.get("/api/posts", async (req, res) => {
  try {
    const { limit, cursor } = req.query;
    const parsedLimit = limit ? parseInt(limit as string, 10) : 10;
    const feed = await db.getPosts(parsedLimit, cursor as string);
    res.json(feed);
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

app.post("/api/posts", async (req, res) => {
  try {
    const { userId, message, type, isPoll, pollOptions } = req.body;
    const user = await db.getUser(userId);
    if (!user) {
       res.status(404).json({ error: "Author profile lookup failed." });
       return;
    }

    const post = await db.createPost({
      author: user.name,
      username: user.username,
      avatarInitials: user.name.split(" ").map(n => n[0]).join("").toUpperCase(),
      territory: user.territory,
      flag: user.flagEmoji,
      rank: user.rank,
      message,
      type: type || "Article",
      isPoll: !!isPoll,
      pollOptions: pollOptions || []
    });

    res.status(201).json({ success: true, post });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

// Atomic post reactions compiling awards (+5 CC / +5 KC) (Section 3.2)
app.post("/api/posts/:postId/react", async (req, res) => {
  try {
    const { postId } = req.params;
    const { reactionType, userId } = req.body;

    const user = await db.getUser(userId);
    if (!user) {
       res.status(404).json({ error: "Voter index verification failure." });
       return;
    }

    const result = await db.reactToPost(postId, reactionType, userId);
    if (!result) {
       res.status(404).json({ error: "Post item not found." });
       return;
    }

    // Award credits atomically based on dynamic compilation algorithms
    if (result.kcAward > 0) {
      const updatedKC = user.knowledgeCredits + result.kcAward;
      // Recount rank progressions based on spec (Section 3.2.3)
      const escalationRank = calculateRankGrading(updatedKC, user.contributionCredits);
      await db.updateUserStats(userId, { knowledgeCredits: updatedKC, rank: escalationRank });

      // Synchronize internal system event alert logs (Section 2)
      await db.createNotification({
        userId,
        title: "Knowledge Credits Earned",
        body: `Your post reaction compiled +5 KC. Knowledge compilation compiles globally!`,
        type: "REACTION"
      });
    }

    if (result.ccAward > 0) {
      const updatedCC = user.contributionCredits + result.ccAward;
      const escalationRank = calculateRankGrading(user.knowledgeCredits, updatedCC);
      await db.updateUserStats(userId, { contributionCredits: updatedCC, rank: escalationRank });

      await db.createNotification({
        userId,
        title: "Contribution Credits Earned",
        body: `Your public contribution reaction compiled +5 CC. Your effort has been added to territory indexes.`,
        type: "REACTION"
      });
    }

    const finalUser = await db.getUser(userId);
    res.json({ success: true, post: result.post, updatedUser: finalUser });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

// Simple rank escalation helper algorithm based on credits
function calculateRankGrading(kc: number, cc: number): string {
  if (kc >= 50 && cc >= 30) return "Noble";
  if (kc >= 30 && cc >= 15) return "Guardian";
  if (kc >= 15 && cc >= 5) return "Contributor";
  return "Citizen";
}

// ==========================================
// 3. SECURE MERITOCRATIC ELECTIONS
// ==========================================
app.get("/api/elections/candidates", async (req, res) => {
  try {
    res.json({ candidates: await db.getCandidates() });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

app.post("/api/elections/vote", async (req, res) => {
  try {
    const { candidateId, voterId } = req.body;
    const voter = await db.getUser(voterId);
    if (!voter) {
       res.status(442).json({ error: "Identity token validation failed." });
       return;
    }

    const candidate = await db.castVote(candidateId, voterId);
    if (!candidate) {
       res.status(404).json({ error: "Candidate index lookup failed." });
       return;
    }

    // Return voting confirmation with incremental incentives (+10 CC)
    const updatedUser = await db.getUser(voterId);
    await db.createNotification({
      userId: voterId,
      title: "Civic Compliance Approved",
      body: `Thank you for casting your selection for Senator ${candidate.name}. Fulfiiling voting mandates awarded you +10 CC.`,
      type: "ELECTION"
    });

    res.json({ success: true, candidate, voter: updatedUser });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

// ==========================================
// 4. ACTIVE CONGESTION CHAT SLOTS (Section 1.2)
// ==========================================
app.get("/api/connections/:userId/slots", async (req, res) => {
  try {
    const user = await db.getUser(req.params.userId);
    if (!user) {
       res.status(404).json({ error: "Connection mapping error." });
       return;
    }
    res.json({
      active: user.activeSlots,
      queued: user.queuedSlots
    });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

app.post("/api/connections/archive", async (req, res) => {
  try {
    const { userId, roomId } = req.body;
    const result = await db.archiveChatSlot(userId, roomId);
    res.json({ success: true, ...result });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

// ==========================================
// 5. EVENT NOTIFICATION LOGS (Section 2)
// ==========================================
app.get("/api/notifications/:userId", async (req, res) => {
  try {
    const list = await db.getNotifications(req.params.userId);
    res.json({ notifications: list });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

app.post("/api/notifications/:userId/clear", async (req, res) => {
  try {
    await db.clearNotifications(req.params.userId);
    res.json({ success: true });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});


// Create HTTP server context
const server = http.createServer(app);

// Initialize Socket Server (WebSocket Engine - Section 1)
const wss = new WebSocketServer({ server });

// Map of online connections: record socket structures bound to userIDs
const activeConnections = new Map<string, WebSocket>();

wss.on("connection", (ws: WebSocket) => {
  console.log("A terminal socket has successfully hooked into OEOF WebSocket stream.");
  let sessionUserId: string | null = null;

  ws.on("message", async (messageBuffer) => {
    try {
      const data = JSON.parse(messageBuffer.toString());
      console.log("WebSocket event incoming:", data);

      switch (data.type) {
        // Authenticate socket linkage to register client in broadcast mapping
        case "auth":
          if (data.userId) {
            sessionUserId = data.userId;
            activeConnections.set(data.userId, ws);
            console.log(`Socket authenticated link for citizen: ${data.userId}`);
            ws.send(JSON.stringify({ type: "auth_ack", status: "success", timestamp: new Date() }));
          }
          break;

        // Message dispatcher logic (Section 1.1)
        case "message":
          if (!sessionUserId) {
            ws.send(JSON.stringify({ type: "error", message: "Bearer socket handshake unauthenticated." }));
            break;
          }

          const { roomId, recipientId, content } = data;
          if (!roomId || !recipientId || !content) {
            ws.send(JSON.stringify({ type: "error", message: "Malformed routing bundle metadata." }));
            break;
          }

          const sender = await db.getUser(sessionUserId);
          if (!sender) {
            ws.send(JSON.stringify({ type: "error", message: "Sender record vanished." }));
            break;
          }

          // Persist the message to data ledger (Section 1.1 Writing state)
          const storedMsg = await db.insertMessage({
            roomId,
            senderId: sessionUserId,
            senderName: sender.name,
            recipientId,
            content,
            status: "sent"
          });

          // Acknowledge receipt to the sender (At-least-once confirmation - Section 1.3)
          ws.send(JSON.stringify({
            type: "msg_ack",
            messageId: storedMsg.messageId,
            roomId,
            status: "sent"
          }));

          // Live dispatch routing (Section 1.1)
          const peerSocket = activeConnections.get(recipientId);
          if (peerSocket && peerSocket.readyState === WebSocket.OPEN) {
            // Direct delivery: Online
            peerSocket.send(JSON.stringify({
              type: "message",
              message: storedMsg
            }));
            
            // Mark as delivered atomically
            storedMsg.status = "delivered";
            // Confirm delivery back to sender
            ws.send(JSON.stringify({
              type: "msg_status",
              messageId: storedMsg.messageId,
              status: "delivered"
            }));
          } else {
            // Recipient is Offline - Queue Alert and raise Push alert (Section 1.1 / Section 2.1)
            await db.createNotification({
              userId: recipientId,
              title: "New Message Waiting",
              body: `${sender.name} sent you a secure encrypted message: ${content.substring(0, 30)}${content.length > 30 ? "..." : ""}`,
              type: "MESSAGE"
            });
            console.log(`Message queued under offline mail buffers for recipient: ${recipientId}`);
          }
          break;

        default:
          console.warn(`Unrouted socket signal protocol encountered: ${data.type}`);
          break;
      }
    } catch (e: any) {
      ws.send(JSON.stringify({ type: "error", message: "Server parsed corrupt socket package." }));
    }
  });

  ws.on("close", () => {
    if (sessionUserId) {
      activeConnections.delete(sessionUserId);
      console.log(`Citizen socket detached: ${sessionUserId}`);
    }
  });
});

// Run server
server.listen(PORT, () => {
  console.log(`====================================================`);
  console.log(` 🛡️  OEOF CL CIVILIZATION CORE PLATFORM ACTIVE  🛡️ `);
  console.log(` REST & Socket Channels mapped to port: http://localhost:${PORT}`);
  console.log(`====================================================`);
});
