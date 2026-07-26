import { TestBed } from '@angular/core/testing';
import { afterEach, describe, expect, it } from 'vitest';
import { Card } from '../../../services/websocket.service';
import { card, mountCard } from './card-display.harness';

/**
 * What colour the frame actually ends up, which only a real browser can answer.
 *
 * <p>No single place decides it. A land's tint is the outcome of a specificity contest between
 * two attribute selectors in the stylesheet, and for a dual land an inline style written by
 * multicolorBackground beats both. The component's getters report the inputs to that contest,
 * not its result, so asserting on them would pass while the card on screen stayed brown. These
 * read the computed gradient back instead.
 *
 * <p>The measure is hue, deliberately. The neutral land tan is itself an orange-brown, so
 * "warmer than the tan" is a test a red-brown passes while still looking brown to a player —
 * it was written that way first and the palette passed it while missing the point. Comparing
 * hue against the matching *spell* frame instead asks the question that was actually asked:
 * is this the colour the card makes mana for.
 */

/** Lands reach the client coloured by colour identity, so `colors` is set even with no mana cost. */
const land = (overrides: Partial<Card>): Card => card({
  type: 'LAND',
  manaCost: null,
  color: null,
  colors: [],
  ...overrides,
});

const basic = (name: string, subtype: string, color: string): Card =>
    land({ name, supertypes: ['BASIC'], subtypes: [subtype], color, colors: [color] });

/** The frame a land of this colour must move toward, and must stay duller than. */
const spell = (color: string): Card => card({
  name: `${color} spell`, type: 'INSTANT', manaCost: '{1}', color, colors: [color],
});

/** Colour identity of nothing at all — the case the plain tan frame still exists for. */
const WASTES = land({ name: 'Wastes', supertypes: ['BASIC'], subtypes: ['WASTES'] });
/** Two basic land types, so colour identity makes it multicoloured and its gradient inline. */
const SACRED_FOUNDRY = land({
  name: 'Sacred Foundry', subtypes: ['MOUNTAIN', 'PLAINS'], color: 'RED', colors: ['RED', 'WHITE'],
});

const BASICS: [string, Card, Card][] = [
  ['Mountain', basic('Mountain', 'MOUNTAIN', 'RED'), spell('RED')],
  ['Island', basic('Island', 'ISLAND', 'BLUE'), spell('BLUE')],
  ['Forest', basic('Forest', 'FOREST', 'GREEN'), spell('GREEN')],
  ['Swamp', basic('Swamp', 'SWAMP', 'BLACK'), spell('BLACK')],
  ['Plains', basic('Plains', 'PLAINS', 'WHITE'), spell('WHITE')],
];

type Rgb = [number, number, number];

const TAN = [[169, 132, 89], [138, 102, 64], [74, 51, 32]] as Rgb[];
/** The two stops multicolorBackground must pick for a land: LAND_COLOR_CSS_MAP's RED and WHITE. */
const EARTHY_RED: Rgb = [125, 58, 44];
const EARTHY_WHITE: Rgb = [191, 167, 113];
/** The same two from COLOR_CSS_MAP — right for a gold spell, far too bright for a land. */
const SPELL_RED: Rgb = [160, 48, 48];
const SPELL_WHITE: Rgb = [240, 230, 178];

function mean(xs: number[]): number {
  return xs.reduce((a, b) => a + b, 0) / xs.length;
}

function hue([r, g, b]: Rgb): number {
  const max = Math.max(r, g, b);
  const delta = max - Math.min(r, g, b);
  if (delta === 0) {
    return 0;
  }
  const sextant = max === r ? (g - b) / delta
      : max === g ? (b - r) / delta + 2
      : (r - g) / delta + 4;
  return (sextant * 60 + 360) % 360;
}

/** Circular mean, so stops either side of 0° average to red rather than to cyan. */
function meanHue(stops: Rgb[]): number {
  const radians = stops.map(s => hue(s) * Math.PI / 180);
  const x = mean(radians.map(Math.cos));
  const y = mean(radians.map(Math.sin));
  return (Math.atan2(y, x) * 180 / Math.PI + 360) % 360;
}

/** Shortest angle between two hues, so 350° and 10° are 20° apart rather than 340°. */
function hueGap(a: number, b: number): number {
  const gap = Math.abs(a - b) % 360;
  return gap > 180 ? 360 - gap : gap;
}

/** Distance between the lightest and darkest channel — how far the frame is from grey. */
function saturation(stops: Rgb[]): number {
  return mean(stops.map(s => Math.max(...s) - Math.min(...s)));
}

function has(stops: Rgb[], wanted: Rgb): boolean {
  return stops.some(s => s.every((c, i) => c === wanted[i]));
}

/** The frame's gradient stops, as the browser resolved them from rules plus any inline style. */
async function frameOf(c: Card): Promise<Rgb[]> {
  // mountCard configures a fresh TestBed, which throws once one has been instantiated — so this
  // resets per mount rather than only per test, letting a test compare two frames to each other.
  TestBed.resetTestingModule();
  const { host } = await mountCard(c);
  const image = getComputedStyle(host).backgroundImage;
  const stops = Array.from(image.matchAll(/rgba?\(\s*(\d+),\s*(\d+),\s*(\d+)/g))
      .map(m => [Number(m[1]), Number(m[2]), Number(m[3])] as Rgb);
  expect(stops.length, `no gradient stops found in "${image}"`).toBeGreaterThan(0);
  return stops;
}

describe('land frame colour', () => {
  afterEach(() => TestBed.resetTestingModule());

  for (const [name, basicLand, matchingSpell] of BASICS) {
    it(`tints a ${name} toward its own colour instead of the neutral tan`, async () => {
      const target = meanHue(await frameOf(matchingSpell));
      const tinted = hueGap(meanHue(await frameOf(basicLand)), target);
      const neutral = hueGap(meanHue(await frameOf(WASTES)), target);

      expect(tinted, `${name} sits ${tinted.toFixed(1)}° from its spell frame, `
          + `the untinted tan ${neutral.toFixed(1)}°`).toBeLessThan(neutral);
    });
  }

  it('keeps a Mountain readable as a land rather than reusing the red spell ramp', async () => {
    const mountain = await frameOf(basic('Mountain', 'MOUNTAIN', 'RED'));
    const bolt = await frameOf(spell('RED'));

    expect(mountain).not.toEqual(bolt);
    // The distinction the muted palette exists to buy: at battlefield zoom the frame is most of
    // what is visible, so the land has to stay the duller of the two rather than merely differ.
    expect(saturation(mountain)).toBeLessThan(saturation(bolt));
  });

  it('leaves a land with no colour identity on the plain tan', async () => {
    expect(await frameOf(WASTES)).toEqual(TAN);
  });

  it('builds a dual land gradient from the earthy palette, not the spell one', async () => {
    const foundry = await frameOf(SACRED_FOUNDRY);

    expect(has(foundry, EARTHY_RED), `expected earthy red in ${JSON.stringify(foundry)}`).toBe(true);
    expect(has(foundry, EARTHY_WHITE)).toBe(true);
    expect(has(foundry, SPELL_RED)).toBe(false);
    expect(has(foundry, SPELL_WHITE)).toBe(false);
  });

  it('still gives a multicoloured spell the saturated palette', async () => {
    const gold = await frameOf(card({
      name: 'Boros Charm', type: 'INSTANT', manaCost: '{R}{W}',
      color: 'RED', colors: ['RED', 'WHITE'],
    }));

    expect(has(gold, SPELL_RED)).toBe(true);
    expect(has(gold, SPELL_WHITE)).toBe(true);
  });
});
