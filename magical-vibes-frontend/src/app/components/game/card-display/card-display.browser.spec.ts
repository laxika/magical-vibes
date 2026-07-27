import { TestBed } from '@angular/core/testing';
import { afterEach, describe, expect, it } from 'vitest';
import { Card } from '../../../services/websocket.service';
import { card, fontSizeOf, mountCard, overflowOf } from './card-display.harness';

/**
 * What the unit tests cannot reach: whether a card's text actually fits the frame once the
 * browser has laid it out.
 *
 * <p>Every fitting bug so far has been a late layout change — a web font swapping in after the
 * fit, a mana symbol claiming its width only once the face that draws it arrived — and jsdom
 * reports every box as zero-sized, so none of them were observable from the default test target.
 * These run in real Chromium against the real component.
 */

/** SOS 6. Text as the engine sends it; one mana symbol, mid-sentence. */
const AJANIS_RESPONSE: Card = card({
  name: "Ajani's Response",
  type: 'INSTANT',
  manaCost: '{4}{W}',
  collectorNumber: '6',
  cardText: 'This spell costs {3} less to cast if it targets a tapped creature.\n'
      + 'Destroy target creature.',
});
const AJANIS_FLAVOR = 'Ajani no longer wanted to swing his axe in anger, '
    + 'but some threats leave no other answer.';

/** SOS 7. Long rules text plus three symbols on its own line — the worst case observed. */
const ANTIQUITIES: Card = card({
  name: 'Antiquities on the Loose',
  type: 'SORCERY',
  manaCost: '{1}{W}{W}',
  collectorNumber: '7',
  keywords: ['FLASHBACK'],
  cardText: 'Create two 2/2 red and white Spirit creature tokens. Then if this spell was cast '
      + 'from anywhere other than your hand, put a +1/+1 counter on each Spirit you control.\n'
      + 'Flashback {4}{W}{W}',
});
const ANTIQUITIES_FLAVOR = 'Raising spirits is easier than taming them.';

/** SOS 17. No symbols at all — the card that never clipped, only ever sat too small. */
const GROUP_PROJECT: Card = card({
  name: 'Group Project',
  type: 'SORCERY',
  manaCost: '{1}{W}',
  collectorNumber: '17',
  cardText: 'Create a 2/2 red and white Spirit creature token.\n'
      + 'Flashback—Tap three untapped creatures you control.',
});
const GROUP_PROJECT_FLAVOR = '"Never be afraid to ask for help. Success is much sweeter when '
    + 'it\'s shared."\n—Quintorius Kand';

const CASES: [string, Card, string][] = [
  ["Ajani's Response", AJANIS_RESPONSE, AJANIS_FLAVOR],
  ['Antiquities on the Loose', ANTIQUITIES, ANTIQUITIES_FLAVOR],
  ['Group Project', GROUP_PROJECT, GROUP_PROJECT_FLAVOR],
  ['a creature whose P/T plate overhangs the box', card({
    name: 'Stirring Hopesinger',
    type: 'CREATURE',
    subtypes: ['BIRD', 'BARD'],
    manaCost: '{2}{W}',
    power: 1,
    toughness: 3,
    keywords: ['FLYING', 'LIFELINK'],
    cardText: 'Flying, lifelink\nRepartee — Whenever you cast an instant or sorcery spell that '
        + 'targets a creature, put a +1/+1 counter on each creature you control.',
  }), 'Though not all her students have wings, she was determined to help them reach new heights.'],
  ['a card with no rules text at all', card({ name: 'Blank', cardText: null }), 'A quiet moment.'],
  ['a wall of text with no flavour room left', card({
    name: 'Verbose Enchantment',
    type: 'ENCHANTMENT',
    manaCost: '{3}{W}{W}',
    cardText: 'When this enchantment enters, draw two cards, then discard a card. Whenever a '
        + 'creature you control attacks alone, put a +1/+1 counter on it and it gains vigilance '
        + 'until end of turn. At the beginning of your end step, if you gained life this turn, '
        + 'each opponent loses {2} life and you scry 2.',
  }), 'Some scribes never learned when to stop writing things down for posterity.'],
];

describe('CardDisplayComponent — rules text fitting in real Chromium', () => {

  afterEach(() => TestBed.resetTestingModule());

  it('runs with a layout engine and the card fonts, or the rest proves nothing', async () => {
    const mounted = await mountCard(AJANIS_RESPONSE);

    // jsdom would report both of these as 0, and every overflow assertion would pass vacuously.
    expect(mounted.textBox.clientHeight).toBeGreaterThan(20);
    expect(mounted.host.getBoundingClientRect().height).toBeCloseTo(231, 0);

    /* And the symbol font has to have actually arrived. A symbol is sized by CSS rather than by
       its glyph, so a Mana that never loaded leaves every element the right size with
       nothing drawn in it — the size assertions elsewhere in this file would all still pass and
       every card would render its costs blank. U+E600 is {W}; asking about the default space
       would not answer the question, since none of Mana's glyphs are outside the private use
       area. */
    expect(document.fonts.check('14px Mana', '\uE600'),
        'the Mana font never loaded — symbols would render as empty discs').toBe(true);

    /* Same question of the set symbols, asked by measuring instead. document.fonts.check is no
       use for Keyrune: symbols.css declares a face that overrides the one inside the stylesheet
       it imports, and with two faces of one family check() answers for the unused upstream one
       as readily as for ours — it reports false while the glyphs draw perfectly. Measuring is
       the better question regardless. A bare `.ss` has no width of its own, so whatever this
       measures is the glyph the font drew; with no face loaded there is nothing to draw. */
    const probe = document.createElement('i');
    probe.className = 'ss ss-som';
    document.body.appendChild(probe);
    const drawn = probe.getBoundingClientRect().width;
    probe.remove();

    expect(drawn, 'the Keyrune font never loaded — set symbols would render blank')
        .toBeGreaterThan(5);
  });

  it.each(CASES)('does not clip %s once flavour arrives', async (_name, subject, flavor) => {
    const mounted = await mountCard(subject);

    // The last thing a real card waits on. Symbols are already drawn; flavour is not.
    mounted.cardData.resolveCardData('SOS', subject.collectorNumber!, flavor);
    await mounted.settle();

    expect(overflowOf(mounted.textBox),
        `text box overflows by this many px after everything resolved`).toBeLessThanOrEqual(0);
  });

  it.each(CASES)('does not clip %s when nothing ever resolves', async (_name, subject) => {
    // The other end of the same race: the set-wide fetch never lands, so no flavour ever comes.
    const mounted = await mountCard(subject);
    expect(overflowOf(mounted.textBox)).toBeLessThanOrEqual(0);
  });

  it.each([
    ['the name plate', '.card-mana-cost'],
    ['rules text', '.card-text'],
  ])('centres a mana symbol on the cap height of %s, not below it', async (_where, selector) => {
    /* A mana symbol reads as a capital and belongs on the midpoint of the capitals beside it.
       `vertical-align: middle` — which is what both this and the <img> before it use — instead
       centres the box on the *x-height* midpoint, and every face this app sets text in has a much
       taller cap than x, so an uncorrected symbol hangs low: 0.125em under the cap midpoint in
       Cinzel, 0.164em in Crimson Text, which is a visible sag at the size a cost is printed.
       symbols.css lifts it back. The two contexts are checked separately because they are different
       faces with different metrics, and one constant serves both. */
    const mounted = await mountCard(ANTIQUITIES);
    const host = mounted.host.querySelector(selector) as HTMLElement;
    const symbol = host.querySelector('.mana-sym') as HTMLElement;
    expect(symbol, `no symbol rendered in ${selector}`).not.toBeNull();

    // The baseline, taken from the rendered line rather than from font metrics: a zero-sized
    // inline-block aligned to the baseline reports exactly where it is.
    const probe = document.createElement('span');
    probe.style.cssText = 'display:inline-block;width:0;height:0;vertical-align:baseline';
    host.appendChild(probe);
    const baseline = probe.getBoundingClientRect().top;
    probe.remove();

    const style = getComputedStyle(host);
    const fontSize = parseFloat(style.fontSize);
    const ctx = document.createElement('canvas').getContext('2d')!;
    ctx.font = `${style.fontWeight} ${style.fontSize} ${style.fontFamily}`;
    ctx.textBaseline = 'alphabetic';
    const capHeight = ctx.measureText('H').actualBoundingBoxAscent;

    const rect = symbol.getBoundingClientRect();
    const sagInEm = ((rect.top + rect.height / 2) - (baseline - capHeight / 2)) / fontSize;

    expect(Math.abs(sagInEm),
        `symbol centre is ${sagInEm.toFixed(3)}em off the cap-height midpoint`)
        .toBeLessThan(0.05);
  });

  it('renders a mana symbol at the size of the text around it', async () => {
    /* Two things at once, because they fail together. The symbol has to be a real glyph — Mana
       is `font-display: block`, so if the face never arrives the element is there at zero width
       and every line measures short — and it has to come out about as wide as the text is tall.
       `.ms-cost` draws its disc at 1.3x the element's own font-size, so an unadjusted symbol is
       a quarter oversized, which is enough to move where a line of rules text wraps. */
    const mounted = await mountCard(ANTIQUITIES);

    const symbols = Array.from(mounted.textBox.querySelectorAll('.mana-sym')) as HTMLElement[];
    expect(symbols.length, 'rules text should have rendered its symbols as glyphs')
        .toBeGreaterThan(0);

    const boxFontSize = fontSizeOf(mounted.textBox);
    for (const symbol of symbols) {
      const { width, height } = symbol.getBoundingClientRect();
      expect(width, 'a symbol had no width, so the line it sits on measures short')
          .toBeGreaterThan(0);
      expect(width / boxFontSize,
          'symbol is not about as wide as the text is tall').toBeGreaterThan(0.85);
      expect(width / boxFontSize).toBeLessThan(1.15);
      expect(height / width, 'symbols are round; this one is not square').toBeCloseTo(1, 1);
    }
  });

  it('spaces every pip of a cost alike, including the one upstream singles out', async () => {
    /* Mana 1.18.0 ships `.ms-2 { margin-left: inherit !important }`, and `inherit` on a margin
       takes the parent element's — so `{2}` is the one symbol of the eighty-three whose leading
       gap is decided by whatever box it happens to render in. In the name plate that box is
       `.card-mana-cost` and the number is its 4px stand-off from the name, which on ICE 171's
       {X}{2}{R}{R} opened a hole in front of the 2 seven times the spacing between the other
       three. symbols.css zeroes it.

       Asserted as "every gap is the same gap" rather than against the 0.07em that file sets,
       so this keeps holding if that value is retuned and still catches any other symbol a
       future Mana release decides to treat specially. */
    const mounted = await mountCard(card({
      name: 'Avalanche', type: 'SORCERY', manaCost: '{X}{2}{R}{R}',
      cardText: 'Destroy X target snow lands.',
    }));
    await mounted.settle();

    const pips = Array.from(
        mounted.host.querySelectorAll('.card-mana-cost .mana-sym')) as HTMLElement[];
    expect(pips.length, 'the cost should have rendered as four symbols').toBe(4);

    const rects = pips.map(pip => pip.getBoundingClientRect());
    const gaps = rects.slice(1).map((rect, i) => rect.left - rects[i].right);
    const shown = gaps.map(gap => gap.toFixed(2)).join(', ');
    for (const gap of gaps) {
      expect(gap, `pips are unevenly spaced; gaps are ${shown}px`).toBeCloseTo(gaps[0], 1);
    }
  });

  it('prints a watermark behind the rules text without displacing it', async () => {
    /* The watermark is the one glyph the frame places itself rather than injecting, and it is
       absolutely positioned — so the two ways it can go wrong are being absent (a name the font
       has no glyph for, or a class binding that never resolved) and being in the flow, where a
       40px mark inside a ~60px box would push every line of rules text off the bottom. */
    const mounted = await mountCard(card({ ...ANTIQUITIES, watermark: 'boros' }));
    const mark = mounted.textBox.querySelector('.watermark') as HTMLElement | null;

    expect(mark, 'no watermark rendered for a card that has one').not.toBeNull();
    expect(mark!.className).toContain('ms-watermark-boros');
    expect(mark!.getBoundingClientRect().width,
        'the watermark glyph has no width, so the font never drew it').toBeGreaterThan(0);
    expect(overflowOf(mounted.textBox),
        'the watermark took space in the flow and pushed the rules text out').toBeLessThanOrEqual(0);

    /* A watermark neither font has a glyph for has to render nothing rather than an empty box —
       and it has to stop rendering when the card changes underneath, which is the branch that
       used to be a fetch and a signal. Kaladesh's `consulate` is one Scryfall emits and Mana
       never drew. */
    mounted.fixture.componentRef.setInput('card', card({ ...ANTIQUITIES, watermark: 'consulate' }));
    await mounted.settle();
    expect(mounted.textBox.querySelector('.watermark')).toBeNull();
  });

  it('watermarks a card with its own set symbol when that is the mark', async () => {
    /* `set` is Scryfall's way of saying "this card is watermarked with its own expansion
       symbol" — not a fixed mark like a guild's, but a different symbol per card, drawn from
       Keyrune rather than Mana. It rendered nothing at all until that font arrived. */
    const mounted = await mountCard(card({ ...ANTIQUITIES, setCode: 'SOM', watermark: 'set' }));
    const mark = mounted.textBox.querySelector('.watermark') as HTMLElement | null;

    expect(mark, 'no watermark rendered for a card marked with its own set symbol').not.toBeNull();
    expect(mark!.className).toContain('ss-som');
    expect(mark!.getBoundingClientRect().width,
        'the set glyph has no width, so Keyrune never drew it').toBeGreaterThan(0);
    expect(overflowOf(mounted.textBox),
        'the watermark took space in the flow and pushed the rules text out').toBeLessThanOrEqual(0);
  });

  it('draws the expansion symbol from the font, not from the network', async () => {
    /* Keyrune covers every set the engine implements, so this is the path essentially every
       card takes. The failure it guards is the quiet one: `.ss` on its own draws the MTG logo,
       so a symbol that resolved to the wrong classes still renders something symbol-shaped in
       the right place. Asserting the set-specific class is what tells the two apart. */
    const mounted = await mountCard(card({ ...ANTIQUITIES, setCode: 'SOM' }));
    const symbol = mounted.host.querySelector('.set-symbol') as HTMLElement | null;

    expect(symbol, 'no set symbol rendered at all').not.toBeNull();
    expect(symbol!.className).toContain('ss-som');
    expect(symbol!.getBoundingClientRect().width,
        'the symbol box has no width').toBeGreaterThan(0);
    expect(symbol!.textContent!.trim(),
        'printed the set code beside a set that has a real symbol').toBe('');
  });

  it('prints the set code for a set printed after the font shipped', async () => {
    /* The whole fallback, now that nothing is fetched: a code Keyrune has never heard of has to
       read as itself rather than as the MTG logo that a bare `.ss` would draw. */
    const mounted = await mountCard(card({ ...ANTIQUITIES, setCode: 'ZZZ' }));
    const symbol = mounted.host.querySelector('.set-symbol') as HTMLElement | null;

    expect(symbol, 'nothing at all rendered where the set symbol goes').not.toBeNull();
    expect(symbol!.className,
        'drew a Keyrune glyph for a set it has no symbol for').not.toContain('ss-');
    expect(symbol!.textContent!.trim()).toBe('ZZZ');
    expect(symbol!.getBoundingClientRect().width,
        'the printed code has no width').toBeGreaterThan(0);
  });

  it('refits when the only thing that changed about a card is its symbols', async () => {
    /* A mana symbol is a glyph drawn by a ::before rule, so it appears in no element's
       textContent: `{T}: Add {B}.` and `{T}: Add {B}{B}.` both render the string ": Add .".
       The fit is guarded by a key built from what the box contains, and a key blind to the
       symbols calls these two identical — so swapping one card for the other on a frame that
       is already mounted (the preview pane does exactly this) keeps the size fitted for the
       first and runs the second off the bottom. */
    const mounted = await mountCard(card({
      name: 'Small Ritual', type: 'INSTANT', manaCost: '{B}',
      cardText: '{T}: Add {B}. '.repeat(12),
    }));

    mounted.fixture.componentRef.setInput('card', card({
      name: 'Small Ritual', type: 'INSTANT', manaCost: '{B}',
      cardText: '{T}: Add {B}{B}{B}{B}. '.repeat(12),
    }));
    await mounted.settle();

    expect(overflowOf(mounted.textBox),
        'the second card kept the size fitted for the first').toBeLessThanOrEqual(0);
  });

  it('refits when flavour text reaches the DOM, not when it reaches the model', async () => {
    /* Reproduces the clipping seen in the running app, where every affected card showed its
       flavour text at exactly the box font size — proof the fitter never sized it, because the
       element did not exist when the fit ran.

       Flavour arrives from a set-wide fetch, so the model has it before any change detection
       pass renders it. A fit landing inside that window — the component refits itself when web
       fonts settle, on its own promise callback — measures a box with no flavour in it and stores
       a key saying flavour is present. The render that follows then matches that key, the guard
       skips the fit, and the flavour is never accounted for at any size.

       Invoking the lifecycle hook directly is the point, not a shortcut: it is a fit that happens
       between the data arriving and the DOM catching up, which is precisely the real sequence. */
    const mounted = await mountCard(ANTIQUITIES);
    await mounted.settle();

    mounted.cardData.resolveCardData('SOS', '7', ANTIQUITIES_FLAVOR);
    mounted.fixture.componentInstance.ngAfterViewChecked();

    await mounted.settle();

    const flavorEl = mounted.textBox.querySelector('.card-flavor-text') as HTMLElement | null;
    expect(flavorEl, 'flavour text should have rendered').not.toBeNull();
    expect(overflowOf(mounted.textBox),
        'flavour reached the DOM after the fit and was never measured').toBeLessThanOrEqual(0);
  });

  it('refits on content arriving in the DOM, with no change detection pass at all', async () => {
    /* The bug that survived three fixes. This app is zoneless — no zone.js, no
       provideZoneChangeDetection — so nothing re-checks a view because a fetch resolved, and
       ngAfterViewChecked fires exactly once per card and then never again. Measured in the running
       app: 13 cards, 13 calls, ever. Every piece of content a card waits on lands after that one
       check, so a fitter driven by the hook alone is fitting a box that has not finished filling
       and can never correct itself.

       Content is appended directly here, with no detectChanges anywhere, because that is the
       distinguishing case: if the fit only happens when Angular checks the view, nothing runs. */
    const mounted = await mountCard(ANTIQUITIES);
    const before = fontSizeOf(mounted.textBox);

    const late = document.createElement('div');
    late.className = 'card-flavor-text';
    late.textContent = 'Flavour text arriving well after the only view check this card ever gets, '
        + 'long enough that the box cannot hold it at the size already chosen.';
    mounted.textBox.appendChild(late);

    // Two frames: one for the observer to fire, one for the fit it schedules.
    for (let i = 0; i < 3; i++) {
      await new Promise<void>(resolve => requestAnimationFrame(() => resolve()));
    }

    expect(overflowOf(mounted.textBox),
        'nothing refitted after content arrived, so the fit is still tied to change detection')
        .toBeLessThanOrEqual(0);

    // Reacting can mean either sizing the new content down or dropping it, so assert that the
    // fitter touched it at all rather than which choice it made. `before` is kept as context:
    // an untouched element and an unchanged size together are the signature of the bug.
    const touched = late.style.fontSize !== '' || late.style.display === 'none';
    expect(touched,
        `fitter never touched content that appeared after the last view check (size still ${before}px)`)
        .toBe(true);
  });

  it('keeps rules text within its configured size range', async () => {
    const mounted = await mountCard(ANTIQUITIES);
    mounted.cardData.resolveCardData('SOS', '7', ANTIQUITIES_FLAVOR);
    await mounted.settle();

    const size = fontSizeOf(mounted.textBox);
    expect(size).toBeGreaterThanOrEqual(7);
    expect(size).toBeLessThanOrEqual(11);
  });

  it('does not leave the box needlessly empty', async () => {
    // The complaint that started this: small text with a third of the box unused. Whatever size
    // it settles on, a visibly larger one has to genuinely overflow — otherwise readable size
    // was left on the table.
    const mounted = await mountCard(GROUP_PROJECT);
    mounted.cardData.resolveCardData('SOS', '17', GROUP_PROJECT_FLAVOR);
    await mounted.settle();

    const settled = fontSizeOf(mounted.textBox);
    if (settled >= 11) {
      return; // Already at the ceiling; nothing was given up.
    }

    mounted.textBox.style.fontSize = (settled + 0.3) + 'px';
    const overflowOneStepUp = overflowOf(mounted.textBox);
    mounted.textBox.style.fontSize = settled + 'px';

    expect(overflowOneStepUp,
        `settled on ${settled}px but ${settled + 0.3}px would also have fit`).toBeGreaterThan(0);
  });

  it('drops flavour text rather than clipping it when both cannot fit', async () => {
    const mounted = await mountCard(ANTIQUITIES);
    mounted.cardData.resolveCardData('SOS', '7',
        // Far more flavour than any frame could hold beside this much rules text.
        'A quote so long that no legible size could ever hope to contain both it and the rules '
        + 'text above it, which is exactly the case printed cards resolve by leaving the flavour '
        + 'off the card altogether rather than running it off the bottom edge of the frame.');
    await mounted.settle();

    expect(overflowOf(mounted.textBox)).toBeLessThanOrEqual(0);
    const flavorEl = mounted.textBox.querySelector('.card-flavor-text') as HTMLElement | null;
    if (flavorEl) {
      expect(getComputedStyle(flavorEl).display).toBe('none');
    }
  });

  it('shrinks an over-long name instead of ellipsising it', async () => {
    const mounted = await mountCard(card({
      name: 'Antiquities on the Loose',
      manaCost: '{1}{W}{W}',
    }));
    await mounted.settle();

    const nameEl = mounted.host.querySelector('.card-name') as HTMLElement;
    expect(fontSizeOf(nameEl)).toBeLessThan(12);
    expect(nameEl.scrollWidth - nameEl.clientWidth,
        'name is still truncated after shrinking').toBeLessThanOrEqual(1);
  });
});
