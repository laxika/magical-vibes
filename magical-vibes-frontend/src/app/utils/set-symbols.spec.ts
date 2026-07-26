import { describe, expect, it } from 'vitest';
import { setSymbolClasses } from './set-symbols';

describe('setSymbolClasses', () => {

  it('names the Keyrune glyph for a set the font has', () => {
    expect(setSymbolClasses('SOM')).toBe('ss ss-som');
  });

  it('is nothing for a set the font has never heard of', () => {
    /* Not merely "no glyph": a bare `.ss` draws the MTG logo, so returning 'ss' for an unknown
       code would print a real, wrong symbol on the card. Null is what routes it to the Scryfall
       SVG instead, which is the only path that has a symbol for a set printed after the font. */
    expect(setSymbolClasses('a-set-printed-after-this-font')).toBeNull();
  });

  it('is nothing for a card with no set at all', () => {
    expect(setSymbolClasses(null)).toBeNull();
    expect(setSymbolClasses(undefined)).toBeNull();
    expect(setSymbolClasses('')).toBeNull();
  });

  it('takes the set code as the card carries it', () => {
    // Card.setCode is upper case; Keyrune's classes are lower.
    expect(setSymbolClasses(' Som ')).toBe('ss ss-som');
  });

  it('covers the sets whose watermarks sent us looking for this font', () => {
    // The Mirrodin block — the reason the allegiance marks came up in the first place.
    expect(setSymbolClasses('MBS')).toBe('ss ss-mbs');
    expect(setSymbolClasses('NPH')).toBe('ss ss-nph');
  });

  it('covers the newest sets, which is what the SVG fallback exists for', () => {
    /* INR (2025) and ECL (2026) are the two most recent sets the engine implements, and both
       postdate a lot of Keyrune releases. They are in 3.19.0, so nothing fetches for them —
       if a future upgrade of the engine outruns the font, these are the assertions that flip
       and the fallback quietly picks the set up. */
    expect(setSymbolClasses('INR')).toBe('ss ss-inr');
    expect(setSymbolClasses('ECL')).toBe('ss ss-ecl');
  });
});
