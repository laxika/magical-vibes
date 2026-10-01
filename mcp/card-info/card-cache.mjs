import { mkdir, open, readFile, rename, rm, stat, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { compactMtgjsonCards } from './mtgjson.mjs';
import { DEFAULT_MCP_CACHE_DIR } from '../cache-directory.mjs';

const API_ROOT = 'https://api.scryfall.com';
const CACHE_SCHEMA_VERSION = 1;
const LOCK_WAIT_MS = 30_000;
const LOCK_STALE_MS = 120_000;
const LOCK_POLL_MS = 100;
const REQUEST_INTERVAL_MS = 125;
const DEFAULT_COOLDOWN_MS = 60_000;

const MODULE_DIR = path.dirname(fileURLToPath(import.meta.url));
const DEFAULT_RAW_CACHE_DIR = path.resolve(MODULE_DIR, '../../card-data-cache');

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

export class CardInfoError extends Error {
  constructor(message, { fallback = false } = {}) {
    super(message);
    this.fallback = fallback;
  }
}

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
    cacheDir = process.env.CARD_INFO_CACHE_DIR || DEFAULT_MCP_CACHE_DIR,
    fetchImpl = globalThis.fetch,
    now = () => Date.now(),
    rawCacheDir = DEFAULT_RAW_CACHE_DIR,
  } = {}) {
    if (typeof fetchImpl !== 'function') {
      throw new Error('This server requires Node.js with the global fetch API (Node 20+)');
    }
    this.cacheDir = cacheDir;
    this.fetchImpl = fetchImpl;
    this.now = now;
    this.rawCacheDir = rawCacheDir;
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
        `No ${cachedSet.source || 'scryfall'} card found for ${cachedSet.set.toUpperCase()} #${normalizedCollectorNumber}`,
      );
    }
    return {
      ...card,
      source: cachedSet.source || 'scryfall',
      ...(cachedSet.source_date ? { source_date: cachedSet.source_date } : {}),
    };
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
    return this.acquireFileLock(this.lockPath(setCode));
  }

  async acquireFileLock(file) {
    const deadline = this.now() + LOCK_WAIT_MS;
    while (this.now() < deadline) {
      try {
        return await open(file, 'wx');
      } catch (error) {
        if (error?.code !== 'EEXIST') throw error;
        const details = await stat(file).catch(() => null);
        if (details && this.now() - details.mtimeMs > LOCK_STALE_MS) {
          await rm(file, { force: true });
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
    try {
      return await this.downloadScryfallSet(setCode);
    } catch (error) {
      if (!(error instanceof CardInfoError) || !error.fallback) throw error;
      try {
        return await this.downloadMtgjsonSet(setCode);
      } catch (fallbackError) {
        throw new CardInfoError(`${error.message}; MTGJSON fallback failed: ${fallbackError.message}`);
      }
    }
  }

  async downloadMtgjsonSet(setCode) {
    const sourceSet = setCode === 'mb1' ? 'cmb1' : setCode;
    let payload;
    try {
      payload = JSON.parse(await readFile(path.join(this.rawCacheDir, `mtgjson-${sourceSet}.json`), 'utf8'));
    } catch (error) {
      if (error?.code !== 'ENOENT' && !(error instanceof SyntaxError)) throw error;
    }
    if (!payload?.data || !Array.isArray(payload.data.cards)
        || payload.data.code?.toLowerCase() !== sourceSet) {
      payload = await this.fetchJson(`https://mtgjson.com/api/v5/${sourceSet.toUpperCase()}.json`, 'MTGJSON');
    }
    if (payload?.data?.code?.toLowerCase() !== sourceSet || !Array.isArray(payload.data.cards)) {
      throw new CardInfoError('MTGJSON returned a malformed or mismatched set');
    }
    const cards = compactMtgjsonCards(payload.data.cards, setCode);
    if (cards.length === 0) throw new CardInfoError(`MTGJSON returned no English cards for ${setCode.toUpperCase()}`);
    return {
      schema_version: CACHE_SCHEMA_VERSION,
      set: setCode,
      source: 'mtgjson',
      source_date: payload.meta?.date,
      downloaded_at: new Date(this.now()).toISOString(),
      cards,
    };
  }

  async downloadScryfallSet(setCode) {
    const query = new URLSearchParams({
      q: `set:${setCode}`,
      unique: 'prints',
      order: 'set',
    });
    let url = `${API_ROOT}/cards/search?${query}`;
    const cards = [];

    while (url) {
      const page = await this.fetchPage(url);
      if (!Array.isArray(page?.data) || (page.has_more && !page.next_page)) {
        throw new CardInfoError('Scryfall returned a malformed card list', { fallback: true });
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
      source: 'scryfall',
      downloaded_at: new Date(this.now()).toISOString(),
      cards,
    };
  }

  async fetchPage(url) {
    const lockFile = path.join(this.cacheDir, '.scryfall-request.lock');
    const stateFile = path.join(this.cacheDir, '.scryfall-rate.json');
    const lock = await this.acquireFileLock(lockFile);
    if (!lock) throw new CardInfoError('Timed out waiting for Scryfall request lock', { fallback: true });
    try {
      let state = {};
      try {
        state = JSON.parse(await readFile(stateFile, 'utf8'));
      } catch (error) {
        if (error?.code !== 'ENOENT' && !(error instanceof SyntaxError)) throw error;
      }
      if (state.cooldown_until > this.now()) {
        throw new CardInfoError(`Scryfall is cooling down until ${new Date(state.cooldown_until).toISOString()}`, { fallback: true });
      }
      const delay = (state.next_request_at || 0) - this.now();
      if (delay > 0) await sleep(delay);
      await writeFile(stateFile, compactJson({ next_request_at: this.now() + REQUEST_INTERVAL_MS }), 'utf8');
      return await this.fetchJson(url, 'Scryfall', async (response) => {
        if (response.status !== 429 && response.status !== 403) return;
        const retryAfter = response.headers?.get('retry-after');
        const seconds = retryAfter && Number(retryAfter);
        const retryDate = retryAfter && Date.parse(retryAfter);
        const requestedDelay = Number.isFinite(seconds) && seconds >= 0
          ? seconds * 1000 : Number.isFinite(retryDate) ? retryDate - this.now() : 0;
        await writeFile(stateFile, compactJson({
          cooldown_until: this.now() + Math.max(DEFAULT_COOLDOWN_MS, requestedDelay),
        }), 'utf8');
      });
    } finally {
      await lock.close();
      await rm(lockFile, { force: true });
    }
  }

  async fetchJson(url, provider, onResponse = async () => {}) {
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
      throw new CardInfoError(`Could not reach ${provider}: ${error.message}`, { fallback: true });
    }
    await onResponse(response);

    let payload;
    try {
      payload = await response.json();
    } catch {
      throw new CardInfoError(`${provider} returned HTTP ${response.status} with invalid JSON`, {
        fallback: response.ok || response.status === 429 || response.status === 403 || response.status >= 500,
      });
    }
    if (!response.ok || payload?.object === 'error') {
      throw new CardInfoError(`${provider} returned HTTP ${response.status}${payload?.details ? `: ${payload.details}` : ''}`, {
        fallback: response.status === 429 || response.status === 403 || response.status >= 500,
      });
    }
    return payload;
  }
}
