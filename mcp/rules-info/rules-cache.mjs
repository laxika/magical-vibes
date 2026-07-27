import { mkdir, open, readFile, rename, rm, stat, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const RULES_PAGE_URL = 'https://magic.wizards.com/en/rules';
const CACHE_SCHEMA_VERSION = 1;
const CACHE_TTL_MS = 7 * 24 * 60 * 60 * 1000;
const LOCK_WAIT_MS = 30_000;
const LOCK_POLL_MS = 100;
const USER_AGENT = 'magical-vibes-rules-info-mcp/1.0';

const MODULE_DIR = path.dirname(fileURLToPath(import.meta.url));
const DEFAULT_CACHE_DIR = path.join(MODULE_DIR, 'cache');
const CACHE_FILE = 'comprehensive-rules.json';

// "120.1a Damage can't be dealt ..." / "120.1. Objects can deal damage ..." / "120. Damage"
const RULE_LINE = /^(\d{1,3}(?:\.\d+[a-z]{0,2})?)\.?\s+(\S.*)$/;
const RULE_ID = /^\d{1,3}(?:\.\d+[a-z]{0,2})?$/;
const MAX_RELATED = 40;

export class RulesInfoError extends Error {}

/**
 * Accepts the shapes an agent is likely to have written in a comment — "CR 120.1a",
 * "rule 120.1a.", "120.1A" — and returns the canonical id.
 */
export function normalizeRuleId(ruleId) {
  if (typeof ruleId !== 'string' && typeof ruleId !== 'number') {
    throw new RulesInfoError('rule_id must be a string or number');
  }
  const normalized = String(ruleId)
    .trim()
    .replace(/^(?:cr|rule)\s+/i, '')
    .replace(/\.$/, '')
    .toLowerCase();
  if (!RULE_ID.test(normalized)) {
    throw new RulesInfoError(
      `"${ruleId}" is not a rule number. Expected forms: 120, 120.1, or 120.1a`,
    );
  }
  return normalized;
}

export function parentOf(ruleId) {
  const subrule = /^(\d{1,3}\.\d+)[a-z]{1,2}$/.exec(ruleId);
  if (subrule) return subrule[1];
  const numbered = /^(\d{1,3})\.\d+$/.exec(ruleId);
  if (numbered) return numbered[1];
  return null;
}

function isChildOf(candidate, ruleId) {
  return parentOf(candidate) === ruleId;
}

/**
 * What to offer when a cited rule number is gone: the rules that took its place. For a subrule or
 * numbered rule that means its siblings, for a whole section the sections around it.
 */
export function nearbyRuleIds(existingIds, ruleId) {
  const parent = parentOf(ruleId);
  if (parent !== null) {
    return existingIds
      .filter((candidate) => candidate === parent || isChildOf(candidate, parent))
      .slice(0, MAX_RELATED);
  }
  const target = Number(ruleId);
  return existingIds
    .filter((candidate) => parentOf(candidate) === null)
    .sort((left, right) => Math.abs(Number(left) - target) - Math.abs(Number(right) - target))
    .slice(0, 6)
    .sort((left, right) => Number(left) - Number(right));
}

/**
 * The Comprehensive Rules text file is: title, introduction, a table of contents, then the rule
 * body, then a glossary and credits. Section headers ("120. Damage") appear both in the contents
 * and in the body with identical text, so parsing everything before the glossary is safe. Lines
 * that do not open a new rule ("Example: ..." and the occasional indented paragraph) belong to the
 * rule above them.
 */
export function parseRules(text) {
  const lines = text.replace(/^﻿/, '').split(/\r?\n/);
  const glossary = lines.findLastIndex((line) => line.trim() === 'Glossary');
  const body = glossary === -1 ? lines : lines.slice(0, glossary);

  const rules = {};
  let current = null;
  for (const line of body) {
    if (line.trim() === '') continue;
    const match = RULE_LINE.exec(line);
    if (match) {
      current = match[1];
      rules[current] = match[2].trim();
      continue;
    }
    if (current) rules[current] = `${rules[current]}\n${line.trim()}`;
  }

  if (Object.keys(rules).length === 0) {
    throw new RulesInfoError('The Comprehensive Rules download contained no parsable rules');
  }

  const effectiveDate = /These rules are effective as of ([^.]+)\./i.exec(text)?.[1]?.trim();
  return { rules, effective_date: effectiveDate ?? null };
}

async function sleep(milliseconds) {
  await new Promise((resolve) => setTimeout(resolve, milliseconds));
}

function snippet(text, index, radius = 140) {
  const start = Math.max(0, index - radius);
  const end = Math.min(text.length, index + radius);
  return `${start > 0 ? '…' : ''}${text.slice(start, end).replace(/\n/g, ' ')}${
    end < text.length ? '…' : ''
  }`;
}

export class ComprehensiveRulesCache {
  constructor({
    cacheDir = process.env.RULES_INFO_CACHE_DIR || DEFAULT_CACHE_DIR,
    fetchImpl = globalThis.fetch,
    now = () => Date.now(),
    ttlMs = CACHE_TTL_MS,
  } = {}) {
    if (typeof fetchImpl !== 'function') {
      throw new Error('This server requires Node.js with the global fetch API (Node 20+)');
    }
    this.cacheDir = cacheDir;
    this.fetchImpl = fetchImpl;
    this.now = now;
    this.ttlMs = ttlMs;
  }

  cachePath() {
    return path.join(this.cacheDir, CACHE_FILE);
  }

  lockPath() {
    return `${this.cachePath()}.lock`;
  }

  /** Rule text plus the metadata a caller needs to judge whether the answer is current. */
  async getRule(ruleId) {
    const normalized = normalizeRuleId(ruleId);
    const document = await this.getRules();
    const text = document.rules[normalized];
    if (text === undefined) {
      const related = nearbyRuleIds(Object.keys(document.rules), normalized);
      throw new RulesInfoError(
        `Rule ${normalized} does not exist in the Comprehensive Rules effective ${
          document.effective_date ?? 'unknown'
        }. Do not cite it.${related.length > 0 ? ` Rules that do exist nearby: ${related.join(', ')}` : ''}`,
      );
    }
    return {
      rule_id: normalized,
      verified: true,
      text,
      subrules: Object.keys(document.rules).filter((candidate) => isChildOf(candidate, normalized)),
      effective_date: document.effective_date,
    };
  }

  async searchRules(query, limit = 10) {
    if (typeof query !== 'string' || query.trim().length < 3) {
      throw new RulesInfoError('query must be at least 3 characters');
    }
    const needle = query.trim().toLowerCase();
    const document = await this.getRules();
    const matches = [];
    for (const [ruleId, text] of Object.entries(document.rules)) {
      const index = text.toLowerCase().indexOf(needle);
      if (index >= 0) matches.push({ rule_id: ruleId, index, snippet: snippet(text, index) });
    }
    matches.sort((left, right) => left.index - right.index);
    return {
      query: query.trim(),
      total_matches: matches.length,
      effective_date: document.effective_date,
      matches: matches.slice(0, Math.max(1, Math.min(limit, 50))).map(({ index, ...rest }) => rest),
    };
  }

  async getRules({ force = false } = {}) {
    if (!force) {
      const fresh = await this.readCache({ requireFresh: true });
      if (fresh) return fresh;
    }

    await mkdir(this.cacheDir, { recursive: true });
    const lock = await this.acquireLock();
    if (!lock) {
      const stale = await this.readCache({ requireFresh: false });
      if (stale) return stale;
      throw new RulesInfoError('Timed out waiting for the Comprehensive Rules cache');
    }

    try {
      if (!force) {
        const fresh = await this.readCache({ requireFresh: true });
        if (fresh) return fresh;
      }
      let downloaded;
      try {
        downloaded = await this.download();
      } catch (error) {
        // An expired cache still answers better than nothing when Wizards is unreachable.
        const stale = await this.readCache({ requireFresh: false });
        if (stale) return { ...stale, stale: true, refresh_error: error.message };
        throw error;
      }
      await this.writeCache(downloaded);
      return downloaded;
    } finally {
      await lock.close();
      await rm(this.lockPath(), { force: true });
    }
  }

  async readCache({ requireFresh }) {
    try {
      const parsed = JSON.parse(await readFile(this.cachePath(), 'utf8'));
      if (
        parsed.schema_version !== CACHE_SCHEMA_VERSION ||
        !parsed.rules ||
        typeof parsed.rules !== 'object'
      ) {
        return null;
      }
      if (requireFresh) {
        const downloadedAt = Date.parse(parsed.downloaded_at);
        if (!Number.isFinite(downloadedAt) || this.now() - downloadedAt >= this.ttlMs) return null;
      }
      return parsed;
    } catch (error) {
      if (error?.code === 'ENOENT' || error instanceof SyntaxError) return null;
      throw error;
    }
  }

  async acquireLock() {
    const deadline = this.now() + LOCK_WAIT_MS;
    while (this.now() < deadline) {
      try {
        return await open(this.lockPath(), 'wx');
      } catch (error) {
        if (error?.code !== 'EEXIST') throw error;
        const details = await stat(this.lockPath()).catch(() => null);
        if (details && this.now() - details.mtimeMs > LOCK_WAIT_MS) {
          await rm(this.lockPath(), { force: true });
          continue;
        }
        await sleep(LOCK_POLL_MS);
      }
    }
    return null;
  }

  async writeCache(payload) {
    const destination = this.cachePath();
    const temporary = `${destination}.${process.pid}.tmp`;
    await writeFile(temporary, `${JSON.stringify(payload)}\n`, 'utf8');
    await rename(temporary, destination);
  }

  async download() {
    const sourceUrl = await this.findRulesTextUrl();
    const text = await this.fetchText(sourceUrl);
    const { rules, effective_date: effectiveDate } = parseRules(text);
    return {
      schema_version: CACHE_SCHEMA_VERSION,
      source_url: sourceUrl,
      effective_date: effectiveDate,
      downloaded_at: new Date(this.now()).toISOString(),
      rule_count: Object.keys(rules).length,
      rules,
    };
  }

  /** The download URL carries the release date, so it has to be read off the rules page. */
  async findRulesTextUrl() {
    const html = await this.fetchText(RULES_PAGE_URL);
    const match = /href="([^"]*MagicCompRules[^"]*\.txt)"/i.exec(html);
    if (!match) {
      throw new RulesInfoError(
        `No Comprehensive Rules .txt link was found on ${RULES_PAGE_URL}; the page layout may have changed`,
      );
    }
    return new URL(match[1], RULES_PAGE_URL).href;
  }

  async fetchText(url) {
    let response;
    try {
      response = await this.fetchImpl(new URL(url).href, {
        headers: { 'User-Agent': USER_AGENT },
        signal: AbortSignal.timeout(30_000),
      });
    } catch (error) {
      throw new RulesInfoError(`Could not reach ${url}: ${error.message}`);
    }
    if (!response.ok) {
      throw new RulesInfoError(`${url} returned HTTP ${response.status}`);
    }
    return response.text();
  }
}
