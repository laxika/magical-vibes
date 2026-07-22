import assert from 'node:assert/strict';
import { mkdtemp, readFile, rm, utimes } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';
import { compactCard, ScryfallSetCache } from '../card-cache.mjs';

test('compactCard keeps authoring fields and removes noisy Scryfall data', () => {
  const compact = compactCard({
    id: 'large-id',
    name: 'Delver of Secrets // Insectile Aberration',
    set: 'isd',
    collector_number: '51',
    layout: 'transform',
    prices: { usd: '1.00' },
    image_uris: { normal: 'https://example.test/image.jpg' },
    legalities: { standard: 'not_legal' },
    keywords: ['Transform'],
    card_faces: [
      {
        name: 'Delver of Secrets',
        mana_cost: '{U}',
        type_line: 'Creature — Human Wizard',
        oracle_text: 'At the beginning of your upkeep, look at the top card of your library.',
        image_uris: { normal: 'https://example.test/front.jpg' },
      },
      {
        name: 'Insectile Aberration',
        type_line: 'Creature — Human Insect',
        oracle_text: 'Flying',
        power: '3',
        toughness: '2',
      },
    ],
  });

  assert.deepEqual(compact, {
    name: 'Delver of Secrets // Insectile Aberration',
    set: 'isd',
    collector_number: '51',
    layout: 'transform',
    keywords: ['Transform'],
    card_faces: [
      {
        name: 'Delver of Secrets',
        mana_cost: '{U}',
        type_line: 'Creature — Human Wizard',
        oracle_text: 'At the beginning of your upkeep, look at the top card of your library.',
      },
      {
        name: 'Insectile Aberration',
        type_line: 'Creature — Human Insect',
        oracle_text: 'Flying',
        power: '3',
        toughness: '2',
      },
    ],
  });
});

test('getCard downloads every page of a set, caches it, and returns one card', async () => {
  const cacheDir = await mkdtemp(path.join(os.tmpdir(), 'card-info-test-'));
  const calls = [];
  const pages = [
    {
      object: 'list',
      has_more: true,
      next_page: 'https://api.scryfall.com/cards/search?page=2',
      data: [
        {
          name: 'First Card',
          set: 'tst',
          collector_number: '1',
          oracle_text: 'Draw a card.',
          prices: { usd: '9.99' },
        },
      ],
    },
    {
      object: 'list',
      has_more: false,
      data: [
        {
          name: 'Second Card',
          set: 'tst',
          collector_number: '2a',
          oracle_text: 'Deal 2 damage to any target.',
          image_uris: { normal: 'https://example.test/card.jpg' },
        },
      ],
    },
  ];
  const fetchImpl = async (url, options) => {
    calls.push({ url: String(url), options });
    return { ok: true, status: 200, json: async () => pages.shift() };
  };

  try {
    const cache = new ScryfallSetCache({
      cacheDir,
      fetchImpl,
    });
    const card = await cache.getCard('TST', '2A');

    assert.equal(card.name, 'Second Card');
    assert.equal(calls.length, 2);
    assert.match(calls[0].url, /q=set%3Atst/);
    assert.equal(calls[0].options.headers['User-Agent'], 'magical-vibes-card-info-mcp/1.0');

    const cacheFile = path.join(cacheDir, 'tst.json');
    const persisted = JSON.parse(await readFile(cacheFile, 'utf8'));
    assert.equal(persisted.cards.length, 2);
    assert.equal(persisted.cards[0].prices, undefined);

    await utimes(cacheFile, new Date('2000-01-01T00:00:00Z'), new Date('2000-01-01T00:00:00Z'));
    const cacheOnly = new ScryfallSetCache({
      cacheDir,
      fetchImpl: async () => assert.fail('permanent disk cache should avoid the network'),
    });
    assert.equal((await cacheOnly.getCard('tst', '1')).name, 'First Card');
  } finally {
    await rm(cacheDir, { recursive: true, force: true });
  }
});
