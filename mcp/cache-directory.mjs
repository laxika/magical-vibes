import os from 'node:os';
import path from 'node:path';

export const DEFAULT_MCP_CACHE_DIR = path.join(os.homedir(), '.magical-vibes-mcp', 'cache');
