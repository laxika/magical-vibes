import assert from 'node:assert/strict';
import { mkdtemp, readFile, rm, writeFile } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';
import { CardInfoError, ScryfallSetCache } from '../card-cache.mjs';
import { compactMtgjsonCards } from '../mtgjson.mjs';
import { createRequestHandler } from '../server.mjs';

const mtgCard = {
  name: 'Test Creature', number: '2a', language: 'English', layout: 'normal',
  manaCost: '{1}{G}', type: 'Creature — Bear', text: 'Vigilance',
  originalText: 'Incorrect printed text', power: '2', toughness: '2',
  colors: ['G'], colorIdentity: ['G'], keywords: ['Vigilance'],
  prices: { usd: '123' }, identifiers: { scryfallId: 'unused' },
};

function response(payload, status = 200, retryAfter) {
  return {
    ok: status >= 200 && status < 300, status,
    headers: { get: () => retryAfter ?? null },
    json: async () => payload,
  };
}

function mtgSet(set = 'TST', cards = [mtgCard]) {
  return { meta: { date: '2026-09-07' }, data: { code: set, cards } };
}

async function temporaryCache(t) {
  const cacheDir = await mkdtemp(path.join(os.tmpdir(), 'card-fallback-test-'));
  t.after(() => rm(cacheDir, { recursive: true, force: true }));
  return { cacheDir, rawCacheDir: cacheDir };
}

test('429 falls back through the real MCP handler and persists a compact MTGJSON set', async (t) => {
  const options = await temporaryCache(t);
  const calls = [];
  const cache = new ScryfallSetCache({ ...options, fetchImpl: async (url) => {
    calls.push(url);
    return url.startsWith('https://api.scryfall.com')
      ? response({ details: 'Slow down' }, 429, '120') : response(mtgSet());
  } });
  const result = await createRequestHandler(cache)({ method: 'tools/call', params: {
    name: 'get_card', arguments: { set_code: 'TST', collector_number: '2A' },
  } });
  assert.equal(result.isError, false);
  assert.deepEqual(JSON.parse(result.content[0].text), {
    name: 'Test Creature', mana_cost: '{1}{G}', type_line: 'Creature — Bear',
    oracle_text: 'Vigilance', power: '2', toughness: '2', colors: ['G'],
    set: 'tst', collector_number: '2a', layout: 'normal', color_identity: ['G'],
    keywords: ['Vigilance'], source: 'mtgjson', source_date: '2026-09-07',
  });
  assert.equal(calls.length, 2);
  assert.equal(calls[1], 'https://mtgjson.com/api/v5/TST.json');
  const persisted = JSON.parse(await readFile(path.join(options.cacheDir, 'tst.json'), 'utf8'));
  assert.equal(persisted.source, 'mtgjson');
  assert.equal(persisted.cards[0].prices, undefined);
  const cacheOnly = new ScryfallSetCache({ ...options, fetchImpl: async () => assert.fail('cache should avoid network') });
  assert.equal((await cacheOnly.getCard('tst', '2a')).source, 'mtgjson');
});

test('cooldown shares Retry-After across instances and different sets, then permits Scryfall again', async (t) => {
  const options = await temporaryCache(t);
  let now = Date.now();
  let scryfallCalls = 0;
  const fetchImpl = async (url) => {
    if (url.startsWith('https://api.scryfall.com')) {
      scryfallCalls++;
      if (scryfallCalls === 1) return response({}, 429, new Date(now + 120_000).toUTCString());
      return response({ data: [{ name: 'Recovered', set: 'end', collector_number: '1' }] });
    }
    return response(mtgSet(url.includes('TST') ? 'TST' : 'NEW'));
  };
  await new ScryfallSetCache({ ...options, fetchImpl, now: () => now }).getCard('TST', '2a');
  await new ScryfallSetCache({ ...options, fetchImpl, now: () => now }).getCard('NEW', '2a');
  assert.equal(scryfallCalls, 1);
  const state = JSON.parse(await readFile(path.join(options.cacheDir, '.scryfall-rate.json'), 'utf8'));
  assert.ok(state.cooldown_until >= now + 119_000);
  now += 121_000;
  assert.equal((await new ScryfallSetCache({ ...options, fetchImpl, now: () => now }).getCard('END', '1')).source, 'scryfall');
  assert.equal(scryfallCalls, 2);
});

test('fallback reuses Java MTGJSON disk cache without a second network request', async (t) => {
  const options = await temporaryCache(t);
  await writeFile(path.join(options.rawCacheDir, 'mtgjson-tst.json'), JSON.stringify(mtgSet()));
  let calls = 0;
  const cache = new ScryfallSetCache({ ...options, fetchImpl: async () => {
    calls++;
    if (calls > 1) assert.fail('MTGJSON should be read from disk');
    return response({}, 429);
  } });
  assert.equal((await cache.getCard('tst', '2a')).mana_cost, '{1}{G}');
  assert.equal(calls, 1);
});

test('network errors, server errors, access blocks, and invalid successful JSON fall back', async (t) => {
  for (const failure of ['network', 500, 403, 'invalid-json', 'malformed']) {
    await t.test(String(failure), async (t) => {
      const options = await temporaryCache(t);
      const cache = new ScryfallSetCache({ ...options, fetchImpl: async (url) => {
        if (!url.startsWith('https://api.scryfall.com')) return response(mtgSet());
        if (failure === 'network') throw new Error('connection failed');
        if (failure === 'invalid-json') return { ...response({}), json: async () => { throw new SyntaxError(); } };
        if (failure === 'malformed') return response({});
        return response({}, failure);
      } });
      assert.equal((await cache.getCard('tst', '2a')).source, 'mtgjson');
    });
  }
});

test('ordinary Scryfall lookup errors do not trigger fallback', async (t) => {
  for (const status of [400, 404]) {
    const options = await temporaryCache(t);
    let calls = 0;
    const cache = new ScryfallSetCache({ ...options, fetchImpl: async () => {
      calls++;
      return response({ details: 'Invalid set' }, status);
    } });
    await assert.rejects(cache.getCard('tst', '2a'), new RegExp(`Scryfall returned HTTP ${status}`));
    assert.equal(calls, 1);
  }
});

test('both-provider failure reports both errors and never persists a partial set', async (t) => {
  const options = await temporaryCache(t);
  let calls = 0;
  const cache = new ScryfallSetCache({ ...options, fetchImpl: async () => {
    calls++;
    if (calls === 1) return response({ data: [{ name: 'Partial', collector_number: '1' }],
      has_more: true, next_page: 'https://api.scryfall.com/cards/search?page=2' });
    return response({ details: 'Unavailable' }, calls === 2 ? 429 : 503);
  } });
  await assert.rejects(cache.getCard('tst', '1'), /Scryfall returned HTTP 429.*MTGJSON fallback failed: MTGJSON returned HTTP 503/);
  await assert.rejects(readFile(path.join(options.cacheDir, 'tst.json')), { code: 'ENOENT' });
  await assert.rejects(readFile(path.join(options.cacheDir, 'tst.json.lock')), { code: 'ENOENT' });
});

test('concurrent cache instances pace requests for different sets', async (t) => {
  const options = await temporaryCache(t);
  const times = [];
  const fetchImpl = async (url) => {
    times.push(Date.now());
    const set = new URL(url).searchParams.get('q').slice(4);
    return response({ data: [{ name: 'Card', set, collector_number: '1' }] });
  };
  await Promise.all(['one', 'two', 'tri'].map((set) =>
    new ScryfallSetCache({ ...options, fetchImpl }).getCard(set, '1')));
  assert.equal(times.length, 3);
  for (let i = 1; i < times.length; i++) assert.ok(times[i] - times[i - 1] >= 120);
});

test('MTGJSON conversion combines sorted faces, ignores non-English cards, and normalizes loyalty costs', () => {
  const front = { ...mtgCard, name: 'Front // Back', faceName: 'Front', side: 'a',
    layout: 'transform', text: '[+1]: Draw a card.', loyalty: '3' };
  const back = { ...front, faceName: 'Back', side: 'b', manaCost: undefined,
    text: '[−X]: Deal X damage.', loyalty: '5', keywords: ['Flying'] };
  const [card] = compactMtgjsonCards([
    { ...front, language: 'Japanese', text: 'Wrong language' }, back, front,
  ], 'tst');
  assert.equal(card.name, 'Front // Back');
  assert.equal(card.oracle_text, undefined);
  assert.equal(card.mana_cost, undefined);
  assert.deepEqual(card.card_faces.map((face) => face.name), ['Front', 'Back']);
  assert.equal(card.card_faces[0].mana_cost, '{1}{G}');
  assert.equal(card.card_faces[0].oracle_text, '+1: Draw a card.');
  assert.equal(card.card_faces[1].oracle_text, '−X: Deal X damage.');
  assert.deepEqual(card.keywords, ['Vigilance', 'Flying']);
});

test('split, adventure, flip, and prepare printings keep appropriate mana costs; meld backs remain standalone', () => {
  const front = { ...mtgCard, name: 'Front // Back', faceName: 'Front', side: 'a', layout: 'aftermath' };
  const back = { ...front, faceName: 'Back', side: 'b', manaCost: '{R}', type: 'Instant', colors: ['R'] };
  const [split] = compactMtgjsonCards([front, back], 'tst');
  assert.equal(split.mana_cost, '{1}{G}{R}');
  assert.equal(split.layout, 'split');
  assert.deepEqual(split.colors, ['G', 'R']);
  const [adventure] = compactMtgjsonCards([{ ...front, layout: 'adventure' }, back], 'tst');
  assert.equal(adventure.mana_cost, '{1}{G}');
  assert.equal(adventure.type_line, 'Creature — Bear');
  for (const layout of ['flip', 'prepare']) {
    const [card] = compactMtgjsonCards([{ ...front, layout }, { ...back, layout }], 'tst');
    assert.equal(card.mana_cost, '{1}{G}');
    assert.equal(card.type_line, 'Creature — Bear');
    assert.equal(card.card_faces[1].mana_cost, '{R}');
  }
  const [meld] = compactMtgjsonCards([{ ...back, layout: 'meld', number: '14b' }], 'tst');
  assert.equal(meld.name, 'Back');
  assert.equal(meld.card_faces, undefined);
});

test('malformed Scryfall pagination and null responses fall back instead of persisting incomplete data', async (t) => {
  for (const payload of [null, { data: [{ name: 'Partial', collector_number: '1' }], has_more: true }]) {
    const options = await temporaryCache(t);
    const cache = new ScryfallSetCache({ ...options, fetchImpl: async (url) =>
      response(url.startsWith('https://api.scryfall.com') ? payload : mtgSet()) });
    const set = await cache.getSet('tst');
    assert.equal(set.source, 'mtgjson');
    assert.deepEqual(set.cards.map((card) => card.name), ['Test Creature']);
  }
});

test('MB1 maps to CMB1 while returning the requested set identity', async (t) => {
  const options = await temporaryCache(t);
  const cache = new ScryfallSetCache({ ...options, fetchImpl: async (url) => {
    if (url.startsWith('https://api.scryfall.com')) return response({}, 429);
    assert.equal(url, 'https://mtgjson.com/api/v5/CMB1.json');
    return response(mtgSet('CMB1'));
  } });
  assert.equal((await cache.getCard('mb1', '2a')).set, 'mb1');
});

test('mismatched MTGJSON sets and missing printings fail explicitly', async (t) => {
  const options = await temporaryCache(t);
  const cache = new ScryfallSetCache({ ...options, fetchImpl: async (url) =>
    url.startsWith('https://api.scryfall.com') ? response({}, 429) : response(mtgSet('BAD')) });
  await assert.rejects(cache.getCard('tst', '2a'), /malformed or mismatched set/);
  await writeFile(path.join(options.rawCacheDir, 'mtgjson-tst.json'), JSON.stringify(mtgSet()));
  await assert.rejects(cache.getCard('tst', '999'), (error) =>
    error instanceof CardInfoError && /No mtgjson card found for TST #999/.test(error.message));
});
