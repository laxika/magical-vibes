import { describe, expect, it } from 'vitest';
import { manaSymbolImg } from './mana-symbol-markup';

describe('manaSymbolImg', () => {

  const html = manaSymbolImg('blob:http://localhost/abc-123', '{W}');

  it('gives the symbol a width before the image loads', () => {
    /* The regression. Sized by height alone, an <img> has no width until it loads and supplies
       an intrinsic ratio, so a line of rules text measures narrower than it renders — 61.7px
       against 94.7px for `Flashback {4}{W}{W}`, on a box 137px wide. CardDisplayComponent fits
       text by measuring that box, so it fitted against the wrong width and the line rewrapped
       out of the frame the moment the symbols painted.

       Either a fixed width or an aspect ratio closes it; the ratio is what the implementation
       uses, so that a height override keeps the symbol square rather than stretching it. */
    expect(html).toMatch(/aspect-ratio\s*:\s*1|width\s*:/);
  });

  it('sizes the symbol to the surrounding text', () => {
    expect(html).toMatch(/height\s*:\s*1em/);
  });

  it('stays square under the height override the cost columns apply', () => {
    // card-browser and deck-builder force `height: 14px !important` on .mana-sym. A hard-coded
    // width would fight that and render the symbol as a rectangle; a ratio follows the height.
    expect(html).not.toMatch(/width\s*:\s*\d/);
  });

  it('carries the class the stylesheets and the fitter hook onto', () => {
    expect(html).toContain('class="mana-sym"');
  });

  it('keeps the symbol readable to assistive tech and as a fallback', () => {
    expect(html).toContain('alt="{W}"');
  });

  it('renders the given url', () => {
    expect(html).toContain('src="blob:http://localhost/abc-123"');
  });

  it('is a single self-contained element', () => {
    expect(html.match(/<img/g)).toHaveLength(1);
    expect(html.startsWith('<img')).toBe(true);
    expect(html.endsWith('>')).toBe(true);
  });
});
