import assert from 'node:assert/strict';
import { mkdtemp, readFile, rm } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';
import {
  ComprehensiveRulesCache,
  nearbyRuleIds,
  normalizeRuleId,
  parseRules,
  RulesInfoError,
} from '../rules-cache.mjs';

const RULES_PAGE = `<html><body>
  <a href="https://media.wizards.com/2026/downloads/MagicCompRules 20260619.docx">DOCX</a>
  <a href="https://media.wizards.com/2026/downloads/MagicCompRules 20260619.txt">TXT</a>
</body></html>`;

// Same shape as the real download: BOM, CRLF, a table of contents that repeats the section
// headers, indented continuation paragraphs, examples, and a trailing glossary.
const RULES_TXT = [
  '﻿Magic Comprehensive Rules',
  '',
  'These rules are effective as of June 19, 2026.',
  '',
  'Contents',
  '',
  '1. Game Concepts',
  '120. Damage',
  '',
  'Glossary',
  '',
  'Credits',
  '',
  '1. Game Concepts',
  '',
  '120. Damage',
  '',
  '120.1. Objects can deal damage to battles, creatures, planeswalkers, and players.',
  '',
  "120.1a Damage can't be dealt to an object that's not a battle, a creature, or a planeswalker.",
  '     A second paragraph that belongs to 120.1a.',
  '',
  '120.2. Any object can deal damage.',
  'Example: A creature deals combat damage equal to its power.',
  '',
  'Glossary',
  '',
  'Damage',
  'See rule 120.',
  '',
  'Credits',
].join('\r\n');

function stubFetch(responses) {
  const calls = [];
  return {
    calls,
    fetchImpl: async (url, options) => {
      calls.push({ url: String(url), options });
      const body = responses[String(url)];
      if (body === undefined) return { ok: false, status: 404, text: async () => '' };
      if (body instanceof Error) throw body;
      return { ok: true, status: 200, text: async () => body };
    },
  };
}

const RESPONSES = {
  'https://magic.wizards.com/en/rules': RULES_PAGE,
  'https://media.wizards.com/2026/downloads/MagicCompRules%2020260619.txt': RULES_TXT,
};

async function withCacheDir(run) {
  const cacheDir = await mkdtemp(path.join(os.tmpdir(), 'rules-info-test-'));
  try {
    return await run(cacheDir);
  } finally {
    await rm(cacheDir, { recursive: true, force: true });
  }
}

test('normalizeRuleId accepts the shapes agents write and rejects everything else', () => {
  assert.equal(normalizeRuleId('120.1a'), '120.1a');
  assert.equal(normalizeRuleId('CR 120.1A'), '120.1a');
  assert.equal(normalizeRuleId('rule 509.1.'), '509.1');
  assert.equal(normalizeRuleId(120), '120');
  assert.throws(() => normalizeRuleId('flying'), RulesInfoError);
  assert.throws(() => normalizeRuleId('120.1a.2'), RulesInfoError);
});

test('parseRules splits on rule number, keeps examples and continuations, drops the glossary', () => {
  const { rules, effective_date: effectiveDate } = parseRules(RULES_TXT);

  assert.equal(effectiveDate, 'June 19, 2026');
  assert.equal(rules['120'], 'Damage');
  assert.equal(
    rules['120.1'],
    'Objects can deal damage to battles, creatures, planeswalkers, and players.',
  );
  assert.equal(
    rules['120.1a'],
    "Damage can't be dealt to an object that's not a battle, a creature, or a planeswalker.\n" +
      'A second paragraph that belongs to 120.1a.',
  );
  assert.equal(
    rules['120.2'],
    'Any object can deal damage.\nExample: A creature deals combat damage equal to its power.',
  );
  // "See rule 120." sits under the glossary and must not overwrite the real rule.
  assert.deepEqual(Object.keys(rules).sort(), ['1', '120', '120.1', '120.1a', '120.2']);
});

test('nearbyRuleIds points at the siblings that replaced a dropped rule number', () => {
  const ids = ['119', '120', '120.1', '120.1a', '120.2', '121'];
  assert.deepEqual(nearbyRuleIds(ids, '120.1b'), ['120.1', '120.1a']);
  assert.deepEqual(nearbyRuleIds(ids, '120.9'), ['120', '120.1', '120.2']);
  assert.deepEqual(nearbyRuleIds(ids, '118'), ['119', '120', '121']);
});

test('getRule scrapes the page for the dated txt link, caches it, and reports subrules', async () => {
  await withCacheDir(async (cacheDir) => {
    const { calls, fetchImpl } = stubFetch(RESPONSES);
    const cache = new ComprehensiveRulesCache({ cacheDir, fetchImpl });

    const rule = await cache.getRule('CR 120.1');
    assert.deepEqual(rule, {
      rule_id: '120.1',
      verified: true,
      text: 'Objects can deal damage to battles, creatures, planeswalkers, and players.',
      subrules: ['120.1a'],
      effective_date: 'June 19, 2026',
    });
    assert.deepEqual(calls.map((call) => call.url), [
      'https://magic.wizards.com/en/rules',
      'https://media.wizards.com/2026/downloads/MagicCompRules%2020260619.txt',
    ]);

    const persisted = JSON.parse(
      await readFile(path.join(cacheDir, 'comprehensive-rules.json'), 'utf8'),
    );
    assert.equal(persisted.rule_count, 5);
    assert.equal(
      persisted.source_url,
      'https://media.wizards.com/2026/downloads/MagicCompRules%2020260619.txt',
    );
  });
});

test('a rule number that no longer exists fails loudly and names the rules that do', async () => {
  await withCacheDir(async (cacheDir) => {
    const cache = new ComprehensiveRulesCache({ cacheDir, fetchImpl: stubFetch(RESPONSES).fetchImpl });
    await assert.rejects(cache.getRule('120.1b'), (error) => {
      assert.ok(error instanceof RulesInfoError);
      assert.match(error.message, /Rule 120\.1b does not exist/);
      assert.match(error.message, /effective June 19, 2026/);
      assert.match(error.message, /120\.1, 120\.1a/);
      return true;
    });
  });
});

test('the cache serves reads for seven days and then refreshes', async () => {
  await withCacheDir(async (cacheDir) => {
    const { calls, fetchImpl } = stubFetch(RESPONSES);
    let clock = Date.parse('2026-07-01T00:00:00Z');
    const cache = new ComprehensiveRulesCache({ cacheDir, fetchImpl, now: () => clock });

    await cache.getRule('120.1');
    assert.equal(calls.length, 2);

    clock += 7 * 24 * 60 * 60 * 1000 - 1000;
    await cache.getRule('120.2');
    assert.equal(calls.length, 2, 'still inside the seven-day window');

    clock += 2000;
    await cache.getRule('120.2');
    assert.equal(calls.length, 4, 'past seven days it downloads again');
  });
});

test('an expired cache still answers when Wizards is unreachable', async () => {
  await withCacheDir(async (cacheDir) => {
    let clock = Date.parse('2026-07-01T00:00:00Z');
    const online = stubFetch(RESPONSES);
    const cache = new ComprehensiveRulesCache({
      cacheDir,
      fetchImpl: online.fetchImpl,
      now: () => clock,
    });
    await cache.getRule('120.1');

    clock += 30 * 24 * 60 * 60 * 1000;
    const offline = new ComprehensiveRulesCache({
      cacheDir,
      now: () => clock,
      fetchImpl: async () => {
        throw new Error('getaddrinfo ENOTFOUND magic.wizards.com');
      },
    });

    const document = await offline.getRules();
    assert.equal(document.stale, true);
    assert.match(document.refresh_error, /ENOTFOUND/);
    assert.equal((await offline.getRule('120.1')).text.startsWith('Objects can deal'), true);
  });
});

test('searchRules returns rule numbers with snippets for a phrase', async () => {
  await withCacheDir(async (cacheDir) => {
    const cache = new ComprehensiveRulesCache({ cacheDir, fetchImpl: stubFetch(RESPONSES).fetchImpl });

    const found = await cache.searchRules('deal damage');
    assert.deepEqual(found.matches.map((match) => match.rule_id), ['120.1', '120.2']);
    assert.match(found.matches[0].snippet, /Objects can deal damage/);
    assert.equal(found.total_matches, 2);

    await assert.rejects(cache.searchRules('ab'), RulesInfoError);
  });
});
