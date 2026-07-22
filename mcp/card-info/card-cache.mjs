import { mkdir, open, readFile, rename, rm, stat, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const API_ROOT = 'https://api.scryfall.com';
const CACHE_SCHEMA_VERSION = 1;
const LOCK_WAIT_MS = 30_000;
const LOCK_POLL_MS = 100;

const MODULE_DIR = path.dirname(fileURLToPath(import.meta.url));
const DEFAULT_CACHE_DIR = path.join(MODULE_DIR, 'cache');

const CARD_FIELDS = [
  'name',
  'set',
  'collector_number',
  'layout',
  'mana_cost',
  'type_line',
  'oracle_text',
  'power',
  'toughness',
  'loyalty',
  'defense',
  'colors',
  'color_identity',
  'keywords',
];

const FACE_FIELDS = [
  'name',
  'mana_cost',
  'type_line',
  'oracle_text',
  'power',
  'toughness',
  'loyalty',
  'defense',
  'colors',
];

export class CardInfoError extends Error {}

function copyPresentFields(source, fields) {
  const result = {};
  for (const field of fields) {
    const value = source?.[field];
    if (value !== undefined && value !== null && value !== '') {
      result[field] = value;
    }
  }
  return result;
}

export function compactCard(card) {
  const compact = copyPresentFields(card, CARD_FIELDS);
  if (Array.isArray(card?.card_faces) && card.card_faces.length > 0) {
    compact.card_faces = card.card_faces.map((face) => copyPresentFields(face, FACE_FIELDS));
  }
  return compact;
}

export function normalizeSetCode(setCode) {
  if (typeof setCode !== 'string' || !/^[a-z0-9]{2,8}$/i.test(setCode.trim())) {
    throw new CardInfoError('set_code must contain 2-8 letters or digits');
  }
  return setCode.trim().toLowerCase();
}

export function normalizeCollectorNumber(collectorNumber) {
  if (typeof collectorNumber !== 'string' && typeof collectorNumber !== 'number') {
    throw new CardInfoError('collector_number must be a string or number');
  }
  const normalized = String(collectorNumber).trim();
  if (normalized.length === 0 || normalized.length > 32 || /[\u0000-\u001f\u007f]/.test(normalized)) {
    throw new CardInfoError('collector_number must contain 1-32 printable characters');
  }
  return normalized;
}

function compactJson(value) {
  return JSON.stringify(value);
}

async function sleep(milliseconds) {
  await new Promise((resolve) => setTimeout(resolve, milliseconds));
}

export class ScryfallSetCache {
  constructor({
    cacheDir = process.env.CARD_INFO_CACHE_DIR || DEFAULT_CACHE_DIR,
    fetchImpl = globalThis.fetch,
    now = () => Date.now(),
  } = {}) {
    if (typeof fetchImpl !== 'function') {
      throw new Error('This server requires Node.js with the global fetch API (Node 20+)');
    }
    this.cacheDir = cacheDir;
    this.fetchImpl = fetchImpl;
    this.now = now;
  }

  cachePath(setCode) {
    return path.join(this.cacheDir, `${normalizeSetCode(setCode)}.json`);
  }

  lockPath(setCode) {
    return `${this.cachePath(setCode)}.lock`;
  }

  async getCard(setCode, collectorNumber) {
    const normalizedCollectorNumber = normalizeCollectorNumber(collectorNumber);
    const cachedSet = await this.getSet(setCode);
    const card = cachedSet.cards.find(
      (candidate) =>
        candidate.collector_number?.toLowerCase() === normalizedCollectorNumber.toLowerCase(),
    );
    if (!card) {
      throw new CardInfoError(
        `No Scryfall card found for ${cachedSet.set.toUpperCase()} #${normalizedCollectorNumber}`,
      );
    }
    return card;
  }

  async getSet(setCode) {
    const normalizedSetCode = normalizeSetCode(setCode);
    const cached = await this.readCache(normalizedSetCode);
    if (cached) return cached;

    await mkdir(this.cacheDir, { recursive: true });
    const lock = await this.acquireLock(normalizedSetCode);
    if (!lock) {
      const cached = await this.readCache(normalizedSetCode);
      if (cached) return cached;
      throw new CardInfoError(`Timed out waiting for the ${normalizedSetCode.toUpperCase()} cache`);
    }

    try {
      const cachedAfterLock = await this.readCache(normalizedSetCode);
      if (cachedAfterLock) return cachedAfterLock;
      const downloaded = await this.downloadSet(normalizedSetCode);
      await this.writeCache(normalizedSetCode, downloaded);
      return downloaded;
    } finally {
      await lock.close();
      await rm(this.lockPath(normalizedSetCode), { force: true });
    }
  }

  async readCache(setCode) {
    const file = this.cachePath(setCode);
    try {
      const contents = await readFile(file, 'utf8');
      const parsed = JSON.parse(contents);
      if (
        parsed.schema_version !== CACHE_SCHEMA_VERSION ||
        parsed.set !== setCode ||
        !Array.isArray(parsed.cards)
      ) {
        return null;
      }
      return parsed;
    } catch (error) {
      if (error?.code === 'ENOENT' || error instanceof SyntaxError) return null;
      throw error;
    }
  }

  async acquireLock(setCode) {
    const deadline = this.now() + LOCK_WAIT_MS;
    while (this.now() < deadline) {
      try {
        return await open(this.lockPath(setCode), 'wx');
      } catch (error) {
        if (error?.code !== 'EEXIST') throw error;
        const details = await stat(this.lockPath(setCode)).catch(() => null);
        if (details && this.now() - details.mtimeMs > LOCK_WAIT_MS) {
          await rm(this.lockPath(setCode), { force: true });
          continue;
        }
        await sleep(LOCK_POLL_MS);
      }
    }
    return null;
  }

  async writeCache(setCode, payload) {
    const destination = this.cachePath(setCode);
    const temporary = `${destination}.${process.pid}.tmp`;
    await writeFile(temporary, `${compactJson(payload)}\n`, 'utf8');
    await rename(temporary, destination);
  }

  async downloadSet(setCode) {
    const query = new URLSearchParams({
      q: `set:${setCode}`,
      unique: 'prints',
      order: 'set',
    });
    let url = `${API_ROOT}/cards/search?${query}`;
    const cards = [];

    while (url) {
      const page = await this.fetchPage(url);
      if (!Array.isArray(page.data)) {
        throw new CardInfoError('Scryfall returned a malformed card list');
      }
      cards.push(...page.data.map(compactCard));
      url = page.has_more ? page.next_page : null;
    }

    if (cards.length === 0) {
      throw new CardInfoError(`Scryfall returned no cards for set ${setCode.toUpperCase()}`);
    }

    return {
      schema_version: CACHE_SCHEMA_VERSION,
      set: setCode,
      downloaded_at: new Date(this.now()).toISOString(),
      cards,
    };
  }

  async fetchPage(url) {
    let response;
    try {
      response = await this.fetchImpl(url, {
        headers: {
          Accept: 'application/json;q=0.9,*/*;q=0.8',
          'User-Agent': 'magical-vibes-card-info-mcp/1.0',
        },
        signal: AbortSignal.timeout(20_000),
      });
    } catch (error) {
      throw new CardInfoError(`Could not reach Scryfall: ${error.message}`);
    }

    let payload;
    try {
      payload = await response.json();
    } catch {
      throw new CardInfoError(`Scryfall returned HTTP ${response.status} with invalid JSON`);
    }
    if (!response.ok || payload?.object === 'error') {
      throw new CardInfoError(payload?.details || `Scryfall returned HTTP ${response.status}`);
    }
    return payload;
  }
}
