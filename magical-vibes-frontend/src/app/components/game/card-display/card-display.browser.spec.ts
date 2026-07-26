import { TestBed } from '@angular/core/testing';
import { afterEach, describe, expect, it } from 'vitest';
import { Card } from '../../../services/websocket.service';
import { card, fontSizeOf, mountCard, overflowOf } from './card-display.harness';

/**
 * What the unit tests cannot reach: whether a card's text actually fits the frame once the
 * browser has laid it out.
 *
 * <p>Every fitting bug so far has been a late layout change — a web font swapping in after the
 * fit, a mana symbol claiming width only once its image decoded — and jsdom reports every box as
 * zero-sized, so none of them were observable from the default test target. These run in real
 * Chromium against the real component.
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
  });

  it.each(CASES)('does not clip %s once symbols and flavour arrive', async (_name, subject, flavor) => {
    const mounted = await mountCard(subject);

    // Everything a real card waits on, in the order it arrives.
    mounted.cardData.resolveCardData('SOS', subject.collectorNumber!, flavor);
    mounted.symbols.resolveSymbols();
    await mounted.settle();

    expect(overflowOf(mounted.textBox),
        `text box overflows by this many px after everything resolved`).toBeLessThanOrEqual(0);
  });

  it.each(CASES)('does not clip %s when nothing ever resolves', async (_name, subject) => {
    // The other end of the same race: no flavour, symbols still literal braces.
    const mounted = await mountCard(subject);
    expect(overflowOf(mounted.textBox)).toBeLessThanOrEqual(0);
  });

  it('gives a mana symbol width before it decodes, so the fit is measured against the real line', async () => {
    /* The component fits text by measuring the box during change detection — the same
       synchronous pass that inserts the symbol images, long before any of them has decoded. If a
       symbol has no width until then, the line it sits on measures narrower than it will render,
       the fit is made against the wrong line, and the text rewraps out of the frame once the
       images paint. Nothing fires at that point, so nothing corrects it.

       Asserting on the width rather than on a card overflowing, because whether a given card
       overflows depends on where its wrap boundaries happen to fall; this is the property. */
    const mounted = await mountCard(ANTIQUITIES);
    mounted.symbols.resolveSymbols();

    // Exactly one synchronous pass: symbols in the DOM, none of them decoded.
    mounted.fixture.componentRef.changeDetectorRef.markForCheck();
    mounted.fixture.detectChanges();

    const symbols = Array.from(mounted.textBox.querySelectorAll('img.mana-sym')) as HTMLElement[];
    expect(symbols.length, 'rules text should have rendered its symbols as images')
        .toBeGreaterThan(0);

    const widthsBeforeDecode = symbols.map(s => s.getBoundingClientRect().width);
    const wrapBeforeDecode = mounted.textBox.scrollHeight;

    expect(Math.min(...widthsBeforeDecode),
        'a symbol had zero width before decoding, so the line was measured too narrow')
        .toBeGreaterThan(0);

    await Promise.all(Array.from(mounted.textBox.querySelectorAll('img'))
        .map(img => (img as HTMLImageElement).decode().catch(() => undefined)));

    expect(mounted.textBox.scrollHeight,
        'the text rewrapped once the symbols decoded, after the fit had already been made')
        .toBe(wrapBeforeDecode);
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
    mounted.symbols.resolveSymbols();
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
    mounted.symbols.resolveSymbols();
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
    mounted.symbols.resolveSymbols();
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
    mounted.symbols.resolveSymbols();
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
    mounted.symbols.resolveSymbols();
    await mounted.settle();

    const nameEl = mounted.host.querySelector('.card-name') as HTMLElement;
    expect(fontSizeOf(nameEl)).toBeLessThan(12);
    expect(nameEl.scrollWidth - nameEl.clientWidth,
        'name is still truncated after shrinking').toBeLessThanOrEqual(1);
  });
});
