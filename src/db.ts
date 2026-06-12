import fs from "fs";
import path from "path";
import { v4 as uuidv4 } from "uuid";
import mongoose, { Schema } from "mongoose";
import dotenv from "dotenv";

dotenv.config();

const MONGO_URI = process.env.MONGO_URI || process.env.DATABASE_URL || "";
let isMongo = false;

// Register models
const UserSchema = new Schema({
  userId: { type: String, required: true, unique: true },
  email: { type: String, required: true, unique: true },
  username: { type: String, required: true, unique: true },
  passwordHash: { type: String, required: true },
  name: { type: String, required: true },
  territory: { type: String, required: true },
  flagEmoji: { type: String, required: true },
  rank: { type: String, required: true },
  knowledgeCredits: { type: Number, default: 10 },
  contributionCredits: { type: Number, default: 5 },
  reputation: { type: Number, default: 98 },
  legacyScore: { type: Number, default: 1 },
  personalityTraits: [String],
  followers: [String],
  following: [String],
  activeSlots: [String],
  queuedSlots: [String],
  createdAt: { type: String, default: () => new Date().toISOString() }
});

const PostSchema = new Schema({
  id: { type: String, required: true, unique: true },
  author: { type: String, required: true },
  username: { type: String, required: true },
  avatarInitials: { type: String, required: true },
  territory: { type: String, required: true },
  flag: { type: String, required: true },
  rank: { type: String, required: true },
  message: { type: String, required: true },
  type: { type: String, required: true },
  timeAgo: { type: String, default: "Just now" },
  wiseCount: { type: Number, default: 0 },
  helpfulCount: { type: Number, default: 0 },
  inspiringCount: { type: Number, default: 0 },
  creativeCount: { type: Number, default: 0 },
  valuableCount: { type: Number, default: 0 },
  commentsCount: { type: Number, default: 0 },
  isPoll: { type: Boolean, default: false },
  pollOptions: [String],
  pollVotes: [Number],
  createdAt: { type: String, default: () => new Date().toISOString() }
});

const MessageSchema = new Schema({
  messageId: { type: String, required: true, unique: true },
  roomId: { type: String, required: true },
  senderId: { type: String, required: true },
  senderName: { type: String, required: true },
  recipientId: { type: String, required: true },
  content: { type: String, required: true },
  status: { type: String, enum: ["sent", "delivered", "read"], default: "sent" },
  createdAt: { type: String, default: () => new Date().toISOString() }
});

const CandidateSchema = new Schema({
  id: { type: String, required: true, unique: true },
  name: { type: String, required: true },
  flag: { type: String, required: true },
  rank: { type: String, required: true },
  manifesto: { type: String, required: true },
  votes: { type: Number, default: 0 },
  normalizedKc: { type: Number, default: 0 },
  reputationScore: { type: Number, default: 0 }
});

const NotificationSchema = new Schema({
  id: { type: Number, required: true, unique: true },
  userId: { type: String, required: true },
  title: { type: String, required: true },
  body: { type: String, required: true },
  type: { type: String, required: true },
  isRead: { type: Boolean, default: false },
  createdAt: { type: String, default: () => new Date().toISOString() }
});

export const UserModel = mongoose.models.User || mongoose.model("User", UserSchema);
export const PostModel = mongoose.models.Post || mongoose.model("Post", PostSchema);
export const MessageModel = mongoose.models.Message || mongoose.model("Message", MessageSchema);
export const CandidateModel = mongoose.models.Candidate || mongoose.model("Candidate", CandidateSchema);
export const NotificationModel = mongoose.models.Notification || mongoose.model("Notification", NotificationSchema);

// Define Data Schemas matching Section 3.1
export interface User {
  userId: string;
  email: string;
  username: string;
  passwordHash: string;
  name: string;
  territory: string;
  flagEmoji: string;
  rank: string;
  knowledgeCredits: number;
  contributionCredits: number;
  reputation: number;
  legacyScore: number;
  personalityTraits: string[];
  followers: string[];
  following: string[];
  activeSlots: string[]; // Chat rooms currently active (max 3)
  queuedSlots: string[]; // Queued slot invitations
  createdAt: string;
}

export interface Post {
  id: string;
  author: string;
  username: string;
  avatarInitials: string;
  territory: string;
  flag: string;
  rank: string;
  message: string;
  type: string;
  timeAgo: string;
  wiseCount: number;
  helpfulCount: number;
  inspiringCount: number;
  creativeCount: number;
  valuableCount: number;
  commentsCount: number;
  isPoll: boolean;
  pollOptions: string[];
  pollVotes: number[];
  createdAt: string;
}

export interface Message {
  messageId: string;
  roomId: string;
  senderId: string;
  senderName: string;
  recipientId: string;
  content: string;
  status: "sent" | "delivered" | "read";
  createdAt: string;
}

export interface ChatConnection {
  id: string;
  name: string;
  flag: string;
  rank: string;
  lastMessage: string;
  avatarColor: string;
  isArchived: boolean;
}

export interface Candidate {
  id: string;
  name: string;
  flag: string;
  rank: string;
  manifesto: string;
  votes: number;
  normalizedKc: number;
  reputationScore: number;
}

export interface Notification {
  id: number;
  userId: string;
  title: string;
  body: string;
  type: string;
  isRead: boolean;
  createdAt: string;
}

interface DatabaseSchema {
  users: Record<string, User>;
  posts: Post[];
  messages: Message[];
  candidates: Candidate[];
  notifications: Notification[];
  notificationIdCounter: number;
}

const DB_FILE_PATH = path.join(__dirname, "../data.json");

class DatabaseEngine {
  public data!: DatabaseSchema;

  constructor() {
    this.load();
  }

  // Load database from file or initialize with seed data (Durability Layer)
  private load() {
    if (fs.existsSync(DB_FILE_PATH)) {
      try {
        const raw = fs.readFileSync(DB_FILE_PATH, "utf-8");
        this.data = JSON.parse(raw);
        console.log(`Database loaded successfully from ${DB_FILE_PATH}`);
        return;
      } catch (e) {
        console.error("Error reading database file, re-initializing", e);
      }
    }
    this.initializeSeedData();
  }

  // Persist current state to disk synchronously to prevent loss on crash (Section 4.3)
  public save() {
    try {
      fs.writeFileSync(DB_FILE_PATH, JSON.stringify(this.data, null, 2));
    } catch (e) {
      console.error("Critical error saving data to disk", e);
    }
  }

  private initializeSeedData() {
    this.data = {
      users: {},
      posts: [],
      messages: [],
      candidates: [],
      notifications: [],
      notificationIdCounter: 1000
    };

    // Seed default users
    const defaultUsersList: Partial<User>[] = [
      {
        userId: "user_aryan",
        email: "aryan@oeof.org",
        username: "aryan.s",
        passwordHash: "password123", // Simplified plain storage/hash for direct verification
        name: "Aryan Sharma",
        territory: "India",
        flagEmoji: "🇮🇳",
        rank: "Duke",
        knowledgeCredits: 1530,
        contributionCredits: 820,
        reputation: 98,
        legacyScore: 42,
        personalityTraits: ["Decentralization", "Inclusion", "Open Research"],
        followers: ["user_yumi", "user_elena"],
        following: ["user_yumi", "user_kofi"],
        activeSlots: ["room_aryan_yumi", "room_aryan_elena", "room_aryan_kofi"],
        queuedSlots: ["room_aryan_sophie", "room_aryan_mateo"],
        createdAt: new Date().toISOString()
      },
      {
        userId: "user_yumi",
        email: "yumi@oeof.org",
        username: "yumi.t",
        passwordHash: "password123",
        name: "Yumi Tanaka",
        territory: "Japan",
        flagEmoji: "🇯🇵",
        rank: "Baroness",
        knowledgeCredits: 1240,
        contributionCredits: 750,
        reputation: 96,
        legacyScore: 31,
        personalityTraits: ["Education", "Agricultural Ethics", "Sovereignty"],
        followers: ["user_aryan"],
        following: ["user_aryan"],
        activeSlots: ["room_aryan_yumi"],
        queuedSlots: [],
        createdAt: new Date().toISOString()
      },
      {
        userId: "user_elena",
        email: "elena@oeof.org",
        username: "elena.r",
        passwordHash: "password123",
        name: "Elena Reyes",
        territory: "Spain",
        flagEmoji: "🇪🇸",
        rank: "Noble",
        knowledgeCredits: 950,
        contributionCredits: 620,
        reputation: 94,
        legacyScore: 18,
        personalityTraits: ["Economics", "Civic Assembly", "Digital Library"],
        followers: ["user_aryan"],
        following: ["user_aryan"],
        activeSlots: ["room_aryan_elena"],
        queuedSlots: [],
        createdAt: new Date().toISOString()
      },
      {
        userId: "user_kofi",
        email: "kofi@oeof.org",
        username: "kofi.m",
        passwordHash: "password123",
        name: "Kofi Mensah",
        territory: "Ghana",
        flagEmoji: "🇬🇭",
        rank: "Guardian",
        knowledgeCredits: 710,
        contributionCredits: 580,
        reputation: 95,
        legacyScore: 24,
        personalityTraits: ["Forestry", "Decentralized Energy", "Mutualism"],
        followers: [],
        following: ["user_aryan"],
        activeSlots: ["room_aryan_kofi"],
        queuedSlots: [],
        createdAt: new Date().toISOString()
      },
      {
        userId: "user_sophie",
        email: "sophie@oeof.org",
        username: "sophie.l",
        passwordHash: "password123",
        name: "Sophie Laurent",
        territory: "France",
        flagEmoji: "🇫🇷",
        rank: "Contributor",
        knowledgeCredits: 220,
        contributionCredits: 120,
        reputation: 91,
        legacyScore: 5,
        personalityTraits: ["Neural Networks", "Open-Source AI"],
        followers: [],
        following: [],
        activeSlots: [],
        queuedSlots: ["room_aryan_sophie"],
        createdAt: new Date().toISOString()
      },
      {
        userId: "user_mateo",
        email: "mateo@oeof.org",
        username: "mateo.s",
        passwordHash: "password123",
        name: "Mateo Silva",
        territory: "Brazil",
        flagEmoji: "🇧🇷",
        rank: "Noble",
        knowledgeCredits: 610,
        contributionCredits: 420,
        reputation: 92,
        legacyScore: 14,
        personalityTraits: ["Agronomy", "Alliances", "Ecology"],
        followers: [],
        following: [],
        activeSlots: [],
        queuedSlots: ["room_aryan_mateo"],
        createdAt: new Date().toISOString()
      }
    ];

    for (const u of defaultUsersList) {
      if (u.userId) {
        this.data.users[u.userId] = u as User;
      }
    }

    // Seed default posts matching MainViewModel.kt
    this.data.posts = [
      {
        id: "post_1",
        author: "Aryan Sharma",
        username: "aryan.s",
        avatarInitials: "AS",
        territory: "India",
        flag: "🇮🇳",
        rank: "Duke",
        message: "Just finished designing an open-source decentralized smart-grid framework for rural Territories. This leverages low-cost photovoltaic arrays paired with micro-capacitors to guarantee energy security to 50+ local households. Please review our schematic, peer references are welcome!",
        type: "Project",
        timeAgo: "2h ago",
        wiseCount: 128,
        helpfulCount: 64,
        inspiringCount: 31,
        creativeCount: 12,
        valuableCount: 8,
        commentsCount: 24,
        isPoll: false,
        pollOptions: [],
        pollVotes: [],
        createdAt: new Date(Date.now() - 7200000).toISOString() // 2 hours ago
      },
      {
        id: "post_2",
        author: "Yumi Tanaka",
        username: "yumi.t",
        avatarInitials: "YT",
        territory: "Japan",
        flag: "🇯🇵",
        rank: "Baroness",
        message: "Should the Empire initiate and prioritize a unified digital school in sub-Saharan Territories this season? Cast your votes below. This plan integrates 1,000+ retired educators across Asia and Europe to mentor kids over voice nodes.",
        type: "Civic Poll",
        timeAgo: "5h ago",
        wiseCount: 85,
        helpfulCount: 52,
        inspiringCount: 18,
        creativeCount: 5,
        valuableCount: 11,
        commentsCount: 42,
        isPoll: true,
        pollOptions: ["Yes, allocate global fund", "No, focus on local grids"],
        pollVotes: [72, 28],
        createdAt: new Date(Date.now() - 18000000).toISOString() // 5 hours ago
      },
      {
        id: "post_3",
        author: "Elena Reyes",
        username: "elena.r",
        avatarInitials: "ER",
        territory: "Spain",
        flag: "🇪🇸",
        rank: "Noble",
        message: "The Economics of Open Knowledge: A brief dissertation on why intellectual credits outperform monetary speculation. Knowledge is non-rivalrous; when shared, its systemic utility compiles exponentially.",
        type: "Article",
        timeAgo: "1d ago",
        wiseCount: 210,
        helpfulCount: 98,
        inspiringCount: 45,
        creativeCount: 22,
        valuableCount: 19,
        commentsCount: 14,
        isPoll: false,
        pollOptions: [],
        pollVotes: [],
        createdAt: new Date(Date.now() - 86400000).toISOString() // 1 day ago
      }
    ];

    // Seed messages
    this.data.messages = [
      {
        messageId: "msg_init_1",
        roomId: "room_aryan_yumi",
        senderId: "user_yumi",
        senderName: "Yumi Tanaka",
        recipientId: "user_aryan",
        content: "Aryan, I just sent the finalized manifesto draft for the agricultural project.",
        status: "read",
        createdAt: new Date(Date.now() - 3600000).toISOString()
      },
      {
        messageId: "msg_init_2",
        roomId: "room_aryan_elena",
        senderId: "user_elena",
        senderName: "Elena Reyes",
        recipientId: "user_aryan",
        content: "The solar schools proposal is passing with great statistics! Check India stand.",
        status: "read",
        createdAt: new Date(Date.now() - 7200000).toISOString()
      },
      {
        messageId: "msg_init_3",
        roomId: "room_aryan_kofi",
        senderId: "user_kofi",
        senderName: "Kofi Mensah",
        recipientId: "user_aryan",
        content: "The vote counts in Ghana are looking incredibly strong. We rise!",
        status: "read",
        createdAt: new Date(Date.now() - 10800000).toISOString()
      },
      // Sophie queued request message
      {
        messageId: "msg_queued_1",
        roomId: "room_aryan_sophie",
        senderId: "user_sophie",
        senderName: "Sophie Laurent",
        recipientId: "user_aryan",
        content: "Requested to consult you on neural network models.",
        status: "sent",
        createdAt: new Date().toISOString()
      },
      // Mateo queued request message
      {
        messageId: "msg_queued_2",
        roomId: "room_aryan_mateo",
        senderId: "user_mateo",
        senderName: "Mateo Silva",
        recipientId: "user_aryan",
        content: "Wishes to establish alliance for forestry index.",
        status: "sent",
        createdAt: new Date().toISOString()
      }
    ];

    // Seed election candidates matching design
    this.data.candidates = [
      { id: "cand_aryan", name: "Aryan Sharma", flag: "🇮🇳", rank: "Duke", manifesto: "A future where merit and compassion govern every Territory equally through open blockchain ledgers.", votes: 142, normalizedKc: 92.5, reputationScore: 98.0 },
      { id: "cand_yumi", name: "Yumi Tanaka", flag: "🇯🇵", rank: "Baroness", manifesto: "Knowledge for every citizen, transparency for every Territory, global mentorship classrooms.", votes: 124, normalizedKc: 88.0, reputationScore: 96.0 },
      { id: "cand_elena", name: "Elena Reyes", flag: "🇪🇸", rank: "Noble", manifesto: "Harmonized resource redistribution and digital library access for developing schools.", votes: 85, normalizedKc: 82.3, reputationScore: 94.0 }
    ];

    this.save();
  }
}

class DatabaseService {
  private fileDb = new DatabaseEngine();

  // Fetch a user by id
  async getUser(userId: string): Promise<User | undefined> {
    if (isMongo) {
      const u = await UserModel.findOne({ userId });
      return u ? u.toObject() : undefined;
    }
    return this.fileDb.data.users[userId];
  }

  // Find user by credentials
  async getUserByEmail(email: string): Promise<User | undefined> {
    if (isMongo) {
      const u = await UserModel.findOne({ email });
      return u ? u.toObject() : undefined;
    }
    return Object.values(this.fileDb.data.users).find((u) => u.email === email);
  }

  async getUserByUsername(username: string): Promise<User | undefined> {
    if (isMongo) {
      const u = await UserModel.findOne({ username });
      return u ? u.toObject() : undefined;
    }
    return Object.values(this.fileDb.data.users).find((u) => u.username === username);
  }

  // Auth: Create Citizen
  async createUser(userData: Partial<User>): Promise<User> {
    const userId = userData.userId || `user_${uuidv4().substring(0, 8)}`;
    const defaultUser: User = {
      userId,
      email: userData.email || "",
      username: userData.username || "",
      passwordHash: userData.passwordHash || "password123",
      name: userData.name || "Anonymous Citizen",
      territory: userData.territory || "Global",
      flagEmoji: userData.flagEmoji || "🌐",
      rank: "Citizen",
      knowledgeCredits: 10,
      contributionCredits: 5,
      reputation: 98,
      legacyScore: 1,
      personalityTraits: userData.personalityTraits || [],
      followers: [],
      following: [],
      activeSlots: userData.activeSlots || [],
      queuedSlots: userData.queuedSlots || [],
      createdAt: new Date().toISOString()
    };
    if (isMongo) {
      const u = new UserModel(defaultUser);
      await u.save();
      return defaultUser;
    }
    this.fileDb.data.users[userId] = defaultUser;
    this.fileDb.save();
    return defaultUser;
  }

  // Update user statistics atomically (Section 3.2 Counter updates)
  async updateUserStats(userId: string, updates: Partial<User>): Promise<User | undefined> {
    if (isMongo) {
      const u = await UserModel.findOneAndUpdate({ userId }, { $set: updates }, { new: true });
      return u ? u.toObject() : undefined;
    }
    const user = this.fileDb.data.users[userId];
    if (!user) return undefined;

    // Mutate state atomically
    if (updates.knowledgeCredits !== undefined) {
      user.knowledgeCredits = updates.knowledgeCredits;
    }
    if (updates.contributionCredits !== undefined) {
      user.contributionCredits = updates.contributionCredits;
    }
    if (updates.rank !== undefined) {
      user.rank = updates.rank;
    }
    if (updates.reputation !== undefined) {
      user.reputation = updates.reputation;
    }
    if (updates.legacyScore !== undefined) {
      user.legacyScore = updates.legacyScore;
    }

    this.fileDb.save();
    return user;
  }

  // ----------------- POSTS ------------------
  async getPosts(limit = 10, cursor?: string): Promise<{ posts: Post[]; nextCursor?: string; hasMore: boolean }> {
    if (isMongo) {
      let query: any = {};
      if (cursor) {
        query.createdAt = { $lt: cursor };
      }
      const fetched = await PostModel.find(query)
        .sort({ createdAt: -1 })
        .limit(limit + 1);

      const hasMore = fetched.length > limit;
      const results = hasMore ? fetched.slice(0, limit) : fetched;
      const nextCursor = hasMore ? results[results.length - 1].createdAt : undefined;
      return {
        posts: results.map((p: any) => p.toObject()),
        nextCursor,
        hasMore
      };
    }

    let list = [...this.fileDb.data.posts];
    list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

    if (cursor) {
      const cursorTime = new Date(cursor).getTime();
      list = list.filter((p) => new Date(p.createdAt).getTime() < cursorTime);
    }

    const fetched = list.slice(0, limit);
    const hasMore = list.length > limit;
    const nextCursor = hasMore ? fetched[fetched.length - 1].createdAt : undefined;

    return { posts: fetched, nextCursor, hasMore };
  }

  async createPost(postData: Partial<Post>): Promise<Post> {
    const id = `post_${uuidv4().substring(0, 8)}`;
    const post: Post = {
      id,
      author: postData.author || "Anonymous Scholar",
      username: postData.username || "anon",
      avatarInitials: postData.avatarInitials || "AA",
      territory: postData.territory || "Global",
      flag: postData.flag || "🌐",
      rank: postData.rank || "Citizen",
      message: postData.message || "",
      type: postData.type || "Article",
      timeAgo: "Just now",
      wiseCount: 0,
      helpfulCount: 0,
      inspiringCount: 0,
      creativeCount: 0,
      valuableCount: 0,
      commentsCount: 0,
      isPoll: postData.isPoll || false,
      pollOptions: postData.pollOptions || [],
      pollVotes: postData.isPoll ? Array(postData.pollOptions?.length || 0).fill(0) : [],
      createdAt: new Date().toISOString()
    };
    if (isMongo) {
      const p = new PostModel(post);
      await p.save();
      return post;
    }
    this.fileDb.data.posts.unshift(post);
    this.fileDb.save();
    return post;
  }

  // Atomic compilation of post reactions (Section 3.2)
  async reactToPost(postId: string, reactionType: string, userId: string): Promise<{ post: Post; kcAward: number; ccAward: number } | undefined> {
    if (isMongo) {
      const inc: any = { commentsCount: 1 };
      let kcAward = 0;
      let ccAward = 0;
      switch (reactionType) {
        case "wise":
          inc.wiseCount = 1;
          kcAward = 5;
          break;
        case "helpful":
          inc.helpfulCount = 1;
          ccAward = 5;
          break;
        case "inspiring":
          inc.inspiringCount = 1;
          break;
        case "creative":
          inc.creativeCount = 1;
          break;
        case "valuable":
          inc.valuableCount = 1;
          break;
      }
      const updated = await PostModel.findOneAndUpdate({ id: postId }, { $inc: inc }, { new: true });
      if (!updated) return undefined;
      return { post: updated.toObject(), kcAward, ccAward };
    }

    const post = this.fileDb.data.posts.find((p) => p.id === postId);
    if (!post) return undefined;

    let kcAward = 0;
    let ccAward = 0;

    switch (reactionType) {
      case "wise":
        post.wiseCount += 1;
        kcAward = 5; // Section 3.2 rewards
        break;
      case "helpful":
        post.helpfulCount += 1;
        ccAward = 5;
        break;
      case "inspiring":
        post.inspiringCount += 1;
        break;
      case "creative":
        post.creativeCount += 1;
        break;
      case "valuable":
        post.valuableCount += 1;
        break;
    }

    post.commentsCount += 1; // Trigger a counter compilation
    this.fileDb.save();
    return { post, kcAward, ccAward };
  }

  // ---------------- MESSAGE INFINITY QUEUE (Section 1) ----------------
  async getMessagesInRoom(roomId: string): Promise<Message[]> {
    if (isMongo) {
      const list = await MessageModel.find({ roomId }).sort({ createdAt: 1 });
      return list.map((m: any) => m.toObject());
    }
    return this.fileDb.data.messages
      .filter((m) => m.roomId === roomId)
      .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
  }

  async insertMessage(msg: Partial<Message>): Promise<Message> {
    const message: Message = {
      messageId: `msg_${uuidv4().substring(0, 8)}`,
      roomId: msg.roomId || "",
      senderId: msg.senderId || "",
      senderName: msg.senderName || "Companion",
      recipientId: msg.recipientId || "",
      content: msg.content || "",
      status: msg.status || "sent",
      createdAt: new Date().toISOString()
    };
    if (isMongo) {
      const m = new MessageModel(message);
      await m.save();
      await this.escalateRooms(message.senderId, message.recipientId, message.roomId);
      return message;
    }
    this.fileDb.data.messages.push(message);

    // Dynamic slot escalation (Three-connection Limit: Section 1.2)
    await this.escalateRooms(message.senderId, message.recipientId, message.roomId);

    this.fileDb.save();
    return message;
  }

  private async escalateRooms(senderId: string, recipientId: string, roomId: string): Promise<void> {
    if (isMongo) {
      const sender = await UserModel.findOne({ userId: senderId });
      const recipient = await UserModel.findOne({ userId: recipientId });
      if (!sender || !recipient) return;

      if (!recipient.activeSlots.includes(roomId) && !recipient.queuedSlots.includes(roomId)) {
        if (recipient.activeSlots.length < 3) {
          await UserModel.findOneAndUpdate({ userId: recipientId }, { $push: { activeSlots: roomId } });
        } else {
          await UserModel.findOneAndUpdate({ userId: recipientId }, { $push: { queuedSlots: roomId } });
        }
      }

      if (!sender.activeSlots.includes(roomId) && !sender.queuedSlots.includes(roomId)) {
        if (sender.activeSlots.length < 3) {
          await UserModel.findOneAndUpdate({ userId: senderId }, { $push: { activeSlots: roomId } });
        } else {
          await UserModel.findOneAndUpdate({ userId: senderId }, { $push: { queuedSlots: roomId } });
        }
      }
      return;
    }

    const sender = this.fileDb.data.users[senderId];
    const recipient = this.fileDb.data.users[recipientId];

    if (!sender || !recipient) return;

    // Track active connection list for recipient
    if (!recipient.activeSlots.includes(roomId) && !recipient.queuedSlots.includes(roomId)) {
      if (recipient.activeSlots.length < 3) {
        recipient.activeSlots.push(roomId);
        console.log(`Pushed ${roomId} to ${recipient.name}'s active slots (3 slots max rule).`);
      } else {
        recipient.queuedSlots.push(roomId);
        console.log(`Escalated ${roomId} to ${recipient.name}'s waitlist queue due to slot congestion.`);
      }
    }

    // Track for sender too
    if (!sender.activeSlots.includes(roomId) && !sender.queuedSlots.includes(roomId)) {
      if (sender.activeSlots.length < 3) {
        sender.activeSlots.push(roomId);
      } else {
        sender.queuedSlots.push(roomId);
      }
    }
  }

  // Archive / Terminate a connection slot (Section 1.4)
  async archiveChatSlot(userId: string, roomId: string): Promise<{ active: string[]; queued: string[] }> {
    if (isMongo) {
      const user = await UserModel.findOne({ userId });
      if (!user) return { active: [], queued: [] };

      let active = user.activeSlots.filter((id: string) => id !== roomId);
      let queued = [...user.queuedSlots];

      if (queued.length > 0) {
        const nextRoom = queued.shift();
        if (nextRoom) {
          active.push(nextRoom);
          await this.createNotification({
            userId,
            title: "Queue Connection Slot Active",
            body: "A queued general consultation has been allocated to your active chat shelf.",
            type: "MESSAGE"
          });
        }
      }
      await UserModel.findOneAndUpdate({ userId }, { $set: { activeSlots: active, queuedSlots: queued } });
      return { active, queued };
    }

    const user = this.fileDb.data.users[userId];
    if (!user) return { active: [], queued: [] };

    user.activeSlots = user.activeSlots.filter((id) => id !== roomId);

    // Pull from queue first
    if (user.queuedSlots.length > 0) {
      const nextRoom = user.queuedSlots.shift();
      if (nextRoom) {
        user.activeSlots.push(nextRoom);
        // Fire a database notification
        await this.createNotification({
          userId,
          title: "Queue Connection Slot Active",
          body: "A queued general consultation has been allocated to your active chat shelf.",
          type: "MESSAGE"
        });
      }
    }

    this.fileDb.save();
    return { active: user.activeSlots, queued: user.queuedSlots };
  }

  // ---------------- ELECTIONS (Section 3.1) ----------------
  async getCandidates(): Promise<Candidate[]> {
    if (isMongo) {
      const list = await CandidateModel.find({});
      return list.map((c: any) => c.toObject());
    }
    return this.fileDb.data.candidates;
  }

  async castVote(candidateId: string, voterId: string): Promise<Candidate | undefined> {
    if (isMongo) {
      const c = await CandidateModel.findOneAndUpdate({ id: candidateId }, { $inc: { votes: 1 } }, { new: true });
      const u = await UserModel.findOne({ userId: voterId });
      if (u) {
        await UserModel.findOneAndUpdate({ userId: voterId }, { $inc: { contributionCredits: 10 } });
      }
      return c ? c.toObject() : undefined;
    }

    const candidate = this.fileDb.data.candidates.find((c) => c.id === candidateId);
    const user = this.fileDb.data.users[voterId];
    if (!candidate || !user) return undefined;

    // Increment votes
    candidate.votes += 1;
    // Civic reward compilation: +10 contribution credits (Section 3.2)
    user.contributionCredits += 10;
    this.fileDb.save();
    return candidate;
  }

  // ---------------- NOTIFICATIONS (Section 2) ----------------
  async getNotifications(userId: string): Promise<Notification[]> {
    if (isMongo) {
      const list = await NotificationModel.find({ userId }).sort({ id: -1 });
      return list.map((n: any) => n.toObject());
    }
    return this.fileDb.data.notifications
      .filter((n) => n.userId === userId)
      .sort((a, b) => b.id - a.id);
  }

  async createNotification(notifData: Partial<Notification>): Promise<Notification> {
    const id = isMongo 
      ? (await NotificationModel.countDocuments({})) + 1000 
      : this.fileDb.data.notificationIdCounter++;

    const notif: Notification = {
      id,
      userId: notifData.userId || "",
      title: notifData.title || "Empire Broadcast",
      body: notifData.body || "",
      type: notifData.type || "EMPIRE",
      isRead: false,
      createdAt: new Date().toISOString()
    };

    if (isMongo) {
      const n = new NotificationModel(notif);
      await n.save();
      return notif;
    }

    this.fileDb.data.notifications.unshift(notif);
    this.fileDb.save();
    return notif;
  }

  async clearNotifications(userId: string): Promise<void> {
    if (isMongo) {
      await NotificationModel.deleteMany({ userId });
      return;
    }
    this.fileDb.data.notifications = this.fileDb.data.notifications.filter((n) => n.userId !== userId);
    this.fileDb.save();
  }
}

async function seedMongoIfEmpty() {
  try {
    const count = await UserModel.countDocuments({});
    if (count === 0) {
      console.log("Seeding default system data into MongoDB Atlas...");
      const dbService = new DatabaseService();
      
      // Seed default users
      const usersList = Object.values(dbService["fileDb"].data.users);
      for (const u of usersList) {
        const doc = new UserModel(u);
        await doc.save();
      }

      // Seed default posts
      const postsList = dbService["fileDb"].data.posts;
      for (const p of postsList) {
        const doc = new PostModel(p);
        await doc.save();
      }

      // Seed default messages
      const messagesList = dbService["fileDb"].data.messages;
      for (const m of messagesList) {
        const doc = new MessageModel(m);
        await doc.save();
      }

      // Seed default candidates
      const candidatesList = dbService["fileDb"].data.candidates;
      for (const c of candidatesList) {
        const doc = new CandidateModel(c);
        await doc.save();
      }

      // Seed default notifications
      const notificationsList = dbService["fileDb"].data.notifications;
      for (const n of notificationsList) {
        const doc = new NotificationModel(n);
        await doc.save();
      }

      console.log("MongoDB Atlas initial seeding executed successfully.");
    }
  } catch (err) {
    console.error("Error seeding MongoDB Atlas:", err);
  }
}

if (MONGO_URI) {
  console.log(`Connecting to MongoDB Atlas on endpoint: ${MONGO_URI.substring(0, 45)}...`);
  mongoose.connect(MONGO_URI)
    .then(async () => {
      console.log("MongoDB Atlas connection established successfully!");
      isMongo = true;
      await seedMongoIfEmpty();
    })
    .catch((err) => {
      console.error("MongoDB Atlas connection error, falling back to local JSON engine:", err);
      isMongo = false;
    });
} else {
  console.log("No MongoDB configuration variables found on environment. Utilizing local JSON Database Engine.");
}

export const db = new DatabaseService();
