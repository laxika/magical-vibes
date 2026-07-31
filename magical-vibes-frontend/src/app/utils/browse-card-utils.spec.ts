import { describe, expect, it } from 'vitest';
import { BrowseCardInfo } from '../services/websocket.service';
import { browseInfoToCard } from './browse-card-utils';

/** A minimal browse entry; each test overrides only the fields it is about. */
function info(overrides: Partial<BrowseCardInfo> = {}): BrowseCardInfo {
  return {
    name: 'Abuna Acolyte',
    collectorNumber: '1',
    setCode: 'SOM',
    manaCost: '{1}{W}',
    typeLine: 'Creature — Cat Cleric',
    rarity: 'uncommon',
    power: '1',
    toughness: '1',
    color: 'WHITE',
    colors: ['WHITE'],
    implemented: true,
    cardText: null,
    keywords: [],
    type: 'CREATURE',
    additionalTypes: [],
    supertypes: [],
    subtypes: ['Cat', 'Cleric'],
    loyalty: null,
    watermark: null,
    backFace: null,
    prepareSpell: null,
    promoTypes: [],
    ...overrides,
  };
}

describe('browseInfoToCard', () => {

  it('carries the watermark through, so a browsed card is marked like a played one', () => {
    /* The browser and the deck builder draw the same frame the battlefield does, from the same
       renderer. This adapter used to hardcode watermark: null, which is why every SOM, MBS and
       NPH card browsed with a blank text box while the very same card showed its mark in play. */
    expect(browseInfoToCard(info({ watermark: 'mirran' })).watermark).toBe('mirran');
  });

  it('leaves the cards that have no watermark unmarked, which is nearly all of them', () => {
    expect(browseInfoToCard(info()).watermark).toBeNull();
  });

  it('marks the spell inset on a prepare card too', () => {
    // The inset spell is rendered by the same frame, so it needs the field populated as well.
    const card = browseInfoToCard(info({
      watermark: 'phyrexian',
      prepareSpell: info({ watermark: 'phyrexian' }),
    }));

    expect(card.prepareSpell?.watermark).toBe('phyrexian');
  });
});
