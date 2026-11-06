import assert from 'node:assert/strict';
import { mkdtemp, readFile, rm } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';
import { ScryfallSetCache } from '../card-cache.mjs';
import { mtgjsonRulings } from '../mtgjson.mjs';
import { createRequestHandler } from '../server.mjs';

const SCRYFALL_RULINGS = 'https://api.scryfall.com/cards/tst/2a/rulings';

function response(payload, status = 200, retryAfter) {
  return {
    ok: status >= 200 && status < 300, status,
    headers: { get: () => retryAfter ?? null },
    json: async () => payload,
  };
}

const scryfallSet = { data: [{ name: 'Test Creature', set: 'tst', collector_number: '2a' }] };

const scryfallRulings = {
  object: 'list',
  data: [
    { object: 'ruling', oracle_id: 'unused', source: 'wotc', published_at: '2011-06-01', comment: 'An official ruling.' },
    { object: 'ruling', oracle_id: 'unused', source: 'scryfall', published_at: '2020-01-01', comment: 'A Scryfall note.' },
  ],
};

function mtgCard(number, rulings, extra = {}) {
  return { name: 'Test Creature', number, language: 'English', layout: 'normal', type: 'Creature', rulings, ...extra };
}

const mtgSet = {
  meta: { date: '2026-09-07' },
  data: {
    code: 'TST',
    cards: [
      mtgCard('2a', [{ date: '2011-06-01', text: 'An official ruling.' }]),
      mtgCard('3', []),
    ],
  },
};

async function temporaryCache(t) {
  const cacheDir = await mkdtemp(path.join(os.tmpdir(), 'card-rulings-test-'));
  t.after(() => rm(cacheDir, { recursive: true, force: true }));
  return { cacheDir };
}

test('get_rulings returns compact Scryfall rulings with the card name and caches them', async (t) => {
  const options = await temporaryCache(t);
  const calls = [];
  const cache = new ScryfallSetCache({ ...options, fetchImpl: async (url) => {
    calls.push(url);
    return response(url === SCRYFALL_RULINGS ? scryfallRulings : scryfallSet);
  } });
  const result = await createRequestHandler(cache)({ method: 'tools/call', params: {
    name: 'get_rulings', arguments: { set_code: 'TST', collector_number: '2A' },
  } });
  assert.equal(result.isError, false);
  assert.deepEqual(JSON.parse(result.content[0].text), {
    name: 'Test Creature', set: 'tst', collector_number: '2a', source: 'scryfall',
    rulings: [
      { date: '2011-06-01', source: 'wotc', text: 'An official ruling.' },
      { date: '2020-01-01', source: 'scryfall', text: 'A Scryfall note.' },
    ],
  });
  assert.equal(calls.at(-1), SCRYFALL_RULINGS);
  const cacheOnly = new ScryfallSetCache({ ...options, fetchImpl: async () => assert.fail('cache should avoid network') });
  assert.equal((await cacheOnly.getRulings('tst', '2a')).rulings.length, 2);
});

test('a card without rulings is cached as an empty list', async (t) => {
  const options = await temporaryCache(t);
  let rulingCalls = 0;
  const fetchImpl = async (url) => {
    if (url !== SCRYFALL_RULINGS) return response(scryfallSet);
    rulingCalls++;
    return response({ object: 'list', data: [] });
  };
  assert.deepEqual((await new ScryfallSetCache({ ...options, fetchImpl }).getRulings('tst', '2a')).rulings, []);
  assert.deepEqual((await new ScryfallSetCache({ ...options, fetchImpl }).getRulings('tst', '2a')).rulings, []);
  assert.equal(rulingCalls, 1);
});

test('a Scryfall cooldown falls back to MTGJSON rulings for the whole set', async (t) => {
  const options = await temporaryCache(t);
  const calls = [];
  const cache = new ScryfallSetCache({ ...options, fetchImpl: async (url) => {
    calls.push(url);
    if (url === SCRYFALL_RULINGS) return response({ details: 'Slow down' }, 429, '120');
    return response(url.startsWith('https://api.scryfall.com') ? scryfallSet : mtgSet);
  } });
  assert.deepEqual(await cache.getRulings('tst', '2a'), {
    name: 'Test Creature', set: 'tst', collector_number: '2a', source: 'mtgjson', source_date: '2026-09-07',
    rulings: [{ date: '2011-06-01', source: 'wotc', text: 'An official ruling.' }],
  });
  assert.equal(calls.at(-1), 'https://mtgjson.com/api/v5/TST.json');
  const persisted = JSON.parse(await readFile(path.join(options.cacheDir, 'tst.rulings.json'), 'utf8'));
  assert.deepEqual(Object.keys(persisted.cards).sort(), ['2a', '3']);
});

test('an ordinary Scryfall rulings error is reported and nothing is persisted', async (t) => {
  const options = await temporaryCache(t);
  const cache = new ScryfallSetCache({ ...options, fetchImpl: async (url) =>
    url === SCRYFALL_RULINGS ? response({ details: 'Not found' }, 404) : response(scryfallSet) });
  await assert.rejects(cache.getRulings('tst', '2a'), /Scryfall returned HTTP 404: Not found/);
  await assert.rejects(readFile(path.join(options.cacheDir, 'tst.rulings.json')), { code: 'ENOENT' });
  await assert.rejects(readFile(path.join(options.cacheDir, 'tst.rulings.json.lock')), { code: 'ENOENT' });
});

test('a collector number that is not in the set fails before any rulings lookup', async (t) => {
  const options = await temporaryCache(t);
  const cache = new ScryfallSetCache({ ...options, fetchImpl: async (url) => {
    assert.notEqual(url, 'https://api.scryfall.com/cards/tst/999/rulings');
    return response(scryfallSet);
  } });
  await assert.rejects(cache.getRulings('tst', '999'), /No scryfall card found for TST #999/);
});

test('MTGJSON rulings merge the faces of a printing and skip other languages', () => {
  const ruling = { date: '2023-02-04', text: 'Shared ruling.' };
  const rulings = mtgjsonRulings([
    mtgCard('5', [ruling], { side: 'a' }),
    mtgCard('5', [ruling, { date: '2023-02-05', text: 'Back face ruling.' }], { side: 'b' }),
    mtgCard('5', [{ date: '2023-02-04', text: 'Wrong language' }], { language: 'Japanese' }),
  ]);
  assert.deepEqual(rulings.get('5'), [
    { date: '2023-02-04', source: 'wotc', text: 'Shared ruling.' },
    { date: '2023-02-05', source: 'wotc', text: 'Back face ruling.' },
  ]);
});
