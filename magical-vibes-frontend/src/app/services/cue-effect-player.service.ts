import { Injectable, InjectionToken, inject } from '@angular/core';
import type { CuePlaybackContext, CuePlayer } from './cue-playback.service';
import { CueElementRegistry } from './cue-elements.service';
import { VisualCue } from './websocket.service';
import { DEBUG_FLASH_EFFECT } from '../components/game/cue-overlay/debug-flash.effect';

/** Where a cue's source and target are on screen, in viewport coordinates; re-measured on every read. */
export interface CueRects {
  readonly source: DOMRect | null;
  readonly target: DOMRect | null;
}

/** Draws one frame of an effect. `progress` runs from 0 to 1 over the animation. */
export type CueFrame = (draw: CanvasRenderingContext2D, progress: number) => void;

/** What an effect draws with. Coordinates on the canvas are viewport coordinates, like {@link CueRects}. */
export interface CueEffectContext {
  /** Draws `frame` on the overlay every animation frame for `durationMs` at normal speed;
      resolves when the animation is over or playback is stopped. */
  animate(durationMs: number, frame: CueFrame): Promise<void>;
  signal: AbortSignal;
  speed: number;
}

/** One visual effect. The player picks the first registered effect that matches a cue. */
export interface CueEffect {
  /** Whether this effect draws the cue, given where its source and target are right now. */
  matches(cue: VisualCue, rects: CueRects): boolean;
  /** Plays the effect; resolves when it is done. */
  play(cue: VisualCue, rects: CueRects, context: CueEffectContext): Promise<void>;
}

/** The canvas effects draw on (the effects overlay). */
export interface CueSurface {
  /** Draws `frame` every animation frame for `durationMs`; resolves when done or when `signal` aborts. */
  animate(durationMs: number, frame: CueFrame, signal: AbortSignal): Promise<void>;
}

/** localStorage flag that turns on the debug flash effect (`localStorage.cueDebugEffects = '1'`). */
export const CUE_DEBUG_STORAGE_KEY = 'cueDebugEffects';

function debugEffectsEnabled(): boolean {
  try {
    return globalThis.localStorage?.getItem(CUE_DEBUG_STORAGE_KEY) === '1';
  } catch {
    return false;
  }
}

/** The registered effects, most specific first. Empty by default, so no cue holds the board back. */
export const CUE_EFFECTS = new InjectionToken<readonly CueEffect[]>('CUE_EFFECTS', {
  providedIn: 'root',
  factory: () => debugEffectsEnabled() ? [DEBUG_FLASH_EFFECT] : []
});

/**
 * The cue player behind the playback queue: maps each cue to the first matching effect and plays
 * it on the attached overlay. With no overlay attached, or no effect for a cue, it plays nothing,
 * so the queue applies the state at once.
 */
@Injectable({ providedIn: 'root' })
export class CueEffectPlayer implements CuePlayer {
  private readonly elements = inject(CueElementRegistry);
  private readonly effects = inject(CUE_EFFECTS);
  private surface: CueSurface | null = null;

  /** Makes `surface` the canvas effects draw on; returns the detach call. */
  attach(surface: CueSurface): () => void {
    this.surface = surface;
    return () => {
      if (this.surface === surface) this.surface = null;
    };
  }

  canPlay(cue: VisualCue): boolean {
    return this.surface !== null && this.effectFor(cue, this.rects(cue)) !== null;
  }

  async play(cue: VisualCue, { signal, speed }: CuePlaybackContext): Promise<void> {
    const surface = this.surface;
    const rects = this.rects(cue);
    const effect = surface ? this.effectFor(cue, rects) : null;
    if (!surface || !effect || signal.aborted) return;
    await effect.play(cue, rects, {
      signal,
      speed,
      animate: (durationMs, frame) => surface.animate(durationMs / speed, frame, signal)
    });
  }

  /** Live on-screen rectangles of the cue's source and target. */
  rects(cue: VisualCue): CueRects {
    const elements = this.elements;
    return {
      get source() { return elements.sourceRect(cue); },
      get target() { return elements.targetRect(cue); }
    };
  }

  private effectFor(cue: VisualCue, rects: CueRects): CueEffect | null {
    return this.effects.find(effect => effect.matches(cue, rects)) ?? null;
  }
}
