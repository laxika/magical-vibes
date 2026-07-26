import { describe, expect, it } from 'vitest';
import { manaSymbolHtml, watermarkSymbolClasses } from './mana-symbols';

describe('manaSymbolHtml', () => {

  it('draws a symbol as a disc, not as a bare pictograph', () => {
    /* .ms-cost is the class that puts the coloured circle behind the glyph. Without it the font
       draws only the inner shape — the sun, the droplet, the skull — floating in the text with
       no disc around it, which is not what a mana symbol looks like on any card ever printed. */
    expect(manaSymbolHtml('{W}')).toContain('ms-cost');
  });

  it('carries the class the stylesheets and the fitter hook onto', () => {
    /* Not upstream's: `.mana-sym` is what the card frame's drop shadow, the cost columns' size
       override and the text fitter's symbol count all select on. */
    expect(manaSymbolHtml('{W}')).toContain('mana-sym');
  });

  it('keeps the symbol readable to assistive tech', () => {
    // A glyph drawn by ::before is not in the DOM's text, so without this the element is empty
    // to anything that is not painting the font.
    expect(manaSymbolHtml('{W}')).toContain('aria-label="{W}"');
  });

  it('renders each kind of symbol with the class the font actually has a glyph for', () => {
    // Mostly the symbol lowercased with the slashes removed, but not always, and the exceptions
    // are the whole reason the mapping is a table rather than a rule.
    expect(manaSymbolHtml('{W}')).toContain('ms-w');
    expect(manaSymbolHtml('{13}')).toContain('ms-13');
    expect(manaSymbolHtml('{W/U}')).toContain('ms-wu');       // hybrid
    expect(manaSymbolHtml('{2/W}')).toContain('ms-2w');       // monocoloured hybrid
    expect(manaSymbolHtml('{W/P}')).toContain('ms-wp');       // Phyrexian
    expect(manaSymbolHtml('{B/G/P}')).toContain('ms-bgp');    // hybrid Phyrexian
    expect(manaSymbolHtml('{T}')).toContain('ms-tap');        // not ms-t
    expect(manaSymbolHtml('{Q}')).toContain('ms-untap');      // not ms-q
    expect(manaSymbolHtml('{HW}')).toContain('ms-w ms-half'); // a modifier, not a glyph
  });

  it('replaces every symbol in a line and leaves the rest of it alone', () => {
    const html = manaSymbolHtml('{T}: Add {G}{G}.');
    expect(html.match(/<i /g)).toHaveLength(3);
    expect(html.startsWith('<i ')).toBe(true);
    expect(html).toContain('>: Add <');
    expect(html.endsWith('.')).toBe(true);
  });

  it('leaves a symbol the font cannot draw as the text it came in as', () => {
    /* The failure mode worth having. {C/P} is a real Scryfall symbol with no glyph in Mana
       1.18.0, and every symbol printed after that version will arrive the same way. Emitting the
       markup anyway would give it a class with no ::before content — an empty coloured disc,
       which tells a player nothing. The braces at least still read as what the card says. */
    expect(manaSymbolHtml('Pay {C/P}.')).toBe('Pay {C/P}.');
    expect(manaSymbolHtml('{NOTASYMBOL}')).toBe('{NOTASYMBOL}');
  });

  it('leaves text with no symbols in it untouched', () => {
    expect(manaSymbolHtml('Destroy target creature.')).toBe('Destroy target creature.');
  });
});

describe('watermarkSymbolClasses', () => {

  it('names the watermark glyph for a card that has one', () => {
    expect(watermarkSymbolClasses('azorius')).toBe('ms ms-watermark-azorius');
  });

  it('does not put the cost disc behind a watermark', () => {
    // A watermark is pressed into the parchment, not a token sitting on it.
    expect(watermarkSymbolClasses('boros')).not.toContain('ms-cost');
  });

  it('is nothing at all for the cards that have no watermark, which is nearly all of them', () => {
    expect(watermarkSymbolClasses(null)).toBeNull();
    expect(watermarkSymbolClasses(undefined)).toBeNull();
    expect(watermarkSymbolClasses('')).toBeNull();
  });

  it('is nothing for a watermark the font has no glyph for', () => {
    /* Scryfall's watermark field is an open enum that grows with every set that prints one.
       Returning a class name for one Mana cannot draw would paint an empty box in the middle of
       the rules text; the frame renders no watermark instead, exactly as it did whenever the old
       fetch came back 404. */
    expect(watermarkSymbolClasses('some-set-printed-after-this-font')).toBeNull();
  });

  it('leaves `set` to the other font rather than claiming it', () => {
    /* `set` means "use this card's own expansion symbol" — not a fixed mark, and not a Mana
       glyph. It is null here and answered by setSymbolClasses instead, which is why this is a
       separate case from the one above: the frame does draw it now, just not from this font. */
    expect(watermarkSymbolClasses('set')).toBeNull();
  });

  it('accepts the spelling as it arrives rather than only in lower case', () => {
    expect(watermarkSymbolClasses(' Azorius ')).toBe('ms ms-watermark-azorius');
  });
});
