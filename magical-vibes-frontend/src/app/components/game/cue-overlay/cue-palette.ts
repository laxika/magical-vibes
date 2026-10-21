import { ParticleMode } from './cue-particles';

/**
 * The looks the default effects come in. The five colors each have one (red fire, blue water,
 * black shadow, white light, green nature); colorless, multicolor and hidden sources get the
 * neutral `arcane`. The rest are fixed looks for cues whose meaning matters more than their
 * source: `smoke` for deaths, `heal` and `drain` for life, `gold` for loyalty.
 */
export type CueElement = 'fire' | 'water' | 'shadow' | 'light' | 'nature' | 'arcane' | 'smoke' | 'heal' | 'drain' | 'gold';

export interface CuePalette {
  element: CueElement;
  /** Particle colors, as `rgb(…)`. */
  colors: readonly string[];
  /** Rings, projectile heads and sparks. */
  accent: string;
  /** How the main particles are drawn: dark looks use smoke, bright ones glow. */
  mode: ParticleMode;
  /** Particle acceleration in anchor units per lifetime²: negative rises (fire, smoke), positive falls (leaves). */
  rise: number;
  /** Tint washed over a card the effect hits. */
  wash: string;
}

export const PALETTES: Readonly<Record<CueElement, CuePalette>> = {
  fire: {
    element: 'fire', colors: ['rgb(255, 224, 130)', 'rgb(255, 152, 40)', 'rgb(232, 64, 24)'],
    accent: 'rgb(255, 196, 80)', mode: 'glow', rise: -0.9, wash: 'rgb(255, 110, 30)'
  },
  water: {
    element: 'water', colors: ['rgb(200, 240, 255)', 'rgb(110, 196, 255)', 'rgb(50, 130, 240)'],
    accent: 'rgb(150, 220, 255)', mode: 'glow', rise: 0.2, wash: 'rgb(70, 150, 255)'
  },
  shadow: {
    element: 'shadow', colors: ['rgb(28, 14, 36)', 'rgb(58, 30, 76)', 'rgb(16, 8, 20)'],
    accent: 'rgb(176, 104, 224)', mode: 'smoke', rise: -0.4, wash: 'rgb(30, 10, 40)'
  },
  light: {
    element: 'light', colors: ['rgb(255, 255, 255)', 'rgb(255, 246, 214)', 'rgb(255, 230, 160)'],
    accent: 'rgb(255, 244, 200)', mode: 'glow', rise: -0.6, wash: 'rgb(255, 250, 225)'
  },
  nature: {
    element: 'nature', colors: ['rgb(214, 255, 160)', 'rgb(132, 226, 104)', 'rgb(54, 172, 78)'],
    accent: 'rgb(170, 240, 120)', mode: 'glow', rise: 0.5, wash: 'rgb(80, 200, 90)'
  },
  arcane: {
    element: 'arcane', colors: ['rgb(236, 238, 250)', 'rgb(186, 196, 222)', 'rgb(255, 236, 196)'],
    accent: 'rgb(220, 226, 255)', mode: 'glow', rise: 0, wash: 'rgb(200, 210, 240)'
  },
  smoke: {
    element: 'smoke', colors: ['rgb(96, 96, 102)', 'rgb(70, 70, 76)', 'rgb(128, 128, 134)'],
    accent: 'rgb(170, 170, 176)', mode: 'smoke', rise: -0.6, wash: 'rgb(24, 24, 28)'
  },
  heal: {
    element: 'heal', colors: ['rgb(220, 255, 220)', 'rgb(150, 240, 160)', 'rgb(255, 250, 200)'],
    accent: 'rgb(170, 255, 180)', mode: 'glow', rise: -0.7, wash: 'rgb(150, 240, 160)'
  },
  drain: {
    element: 'drain', colors: ['rgb(120, 16, 24)', 'rgb(80, 10, 30)', 'rgb(150, 30, 40)'],
    accent: 'rgb(230, 60, 70)', mode: 'smoke', rise: 0.6, wash: 'rgb(110, 0, 20)'
  },
  gold: {
    element: 'gold', colors: ['rgb(255, 240, 170)', 'rgb(255, 210, 90)', 'rgb(230, 170, 50)'],
    accent: 'rgb(255, 220, 110)', mode: 'glow', rise: -0.5, wash: 'rgb(255, 200, 80)'
  }
};

const ELEMENT_BY_COLOR: Readonly<Record<string, CueElement>> = {
  WHITE: 'light',
  BLUE: 'water',
  BLACK: 'shadow',
  RED: 'fire',
  GREEN: 'nature'
};

/** The look of a source: its color's element if it has exactly one color, else neutral. */
export function sourcePalette(sourceColors: readonly string[]): CuePalette {
  const element = sourceColors.length === 1 ? ELEMENT_BY_COLOR[sourceColors[0]] : undefined;
  return PALETTES[element ?? 'arcane'];
}

/** The source's look if it has exactly one color, else `fallback` (a colorless bounce still swirls like water). */
export function sourcePaletteOr(sourceColors: readonly string[], fallback: CueElement): CuePalette {
  const palette = sourcePalette(sourceColors);
  return palette.element === 'arcane' ? PALETTES[fallback] : palette;
}
