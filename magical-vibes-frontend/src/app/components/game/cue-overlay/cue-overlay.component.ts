import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { CueEffectPlayer, CueFrame, CueSurface } from '../../../services/cue-effect-player.service';

interface ActiveAnimation {
  frame: CueFrame;
  durationMs: number;
  start: number | null;
  resolve: () => void;
}

/**
 * The effects overlay: one canvas over the whole game view that visual cue effects draw on. It
 * never takes input (`pointer-events: none`), sits under the skip catcher, and draws in viewport
 * coordinates so effects can use `getBoundingClientRect()` rectangles directly — cards keep their
 * own DOM and CSS zoom, and the canvas follows window resizes and the device pixel ratio on every
 * frame. One `requestAnimationFrame` loop runs while at least one animation is playing.
 */
@Component({
  selector: 'app-cue-overlay',
  standalone: true,
  template: '<canvas #canvas class="cue-overlay-canvas" aria-hidden="true"></canvas>',
  styles: [`
    :host {
      position: fixed;
      inset: 0;
      z-index: 899;
      pointer-events: none;
    }
    .cue-overlay-canvas {
      display: block;
      width: 100%;
      height: 100%;
    }
  `]
})
export class CueOverlayComponent implements CueSurface, OnInit, OnDestroy {
  @ViewChild('canvas', { static: true }) private canvasRef!: ElementRef<HTMLCanvasElement>;
  private readonly player = inject(CueEffectPlayer);
  private readonly animations = new Set<ActiveAnimation>();
  private frameRequest: number | null = null;
  private detach: () => void = () => {};

  ngOnInit(): void {
    this.detach = this.player.attach(this);
  }

  ngOnDestroy(): void {
    this.detach();
    this.stopLoop();
    [...this.animations].forEach(animation => this.finish(animation));
  }

  /** Whether the animation loop is running. */
  get animating(): boolean {
    return this.frameRequest !== null;
  }

  animate(durationMs: number, frame: CueFrame, signal: AbortSignal): Promise<void> {
    return new Promise<void>(resolve => {
      if (signal.aborted) {
        resolve();
        return;
      }
      const animation: ActiveAnimation = { frame, durationMs, start: null, resolve };
      this.animations.add(animation);
      signal.addEventListener('abort', () => this.finish(animation), { once: true });
      this.frameRequest ??= requestAnimationFrame(this.tick);
    });
  }

  private readonly tick = (now: number): void => {
    this.frameRequest = null;
    const draw = this.prepareCanvas();
    for (const animation of [...this.animations]) {
      animation.start ??= now;
      const progress = animation.durationMs > 0 ? Math.min(1, (now - animation.start) / animation.durationMs) : 1;
      if (draw) {
        draw.save();
        animation.frame(draw, progress);
        draw.restore();
      }
      if (progress >= 1) this.finish(animation);
    }
    if (this.animations.size > 0) {
      this.frameRequest = requestAnimationFrame(this.tick);
    } else {
      this.clear();
    }
  };

  /** Sizes the backing store to the canvas's CSS size times the pixel ratio, clears it, and maps
      viewport coordinates onto it. */
  private prepareCanvas(): CanvasRenderingContext2D | null {
    const canvas = this.canvasRef.nativeElement;
    const draw = canvas.getContext('2d');
    if (!draw) return null;
    const box = canvas.getBoundingClientRect();
    const ratio = window.devicePixelRatio || 1;
    const width = Math.round(box.width * ratio);
    const height = Math.round(box.height * ratio);
    if (canvas.width !== width || canvas.height !== height) {
      canvas.width = width;
      canvas.height = height;
    }
    draw.setTransform(1, 0, 0, 1, 0, 0);
    draw.clearRect(0, 0, canvas.width, canvas.height);
    draw.setTransform(ratio, 0, 0, ratio, -box.left * ratio, -box.top * ratio);
    return draw;
  }

  private clear(): void {
    const canvas = this.canvasRef.nativeElement;
    const draw = canvas.getContext('2d');
    draw?.setTransform(1, 0, 0, 1, 0, 0);
    draw?.clearRect(0, 0, canvas.width, canvas.height);
  }

  private finish(animation: ActiveAnimation): void {
    if (!this.animations.delete(animation)) return;
    animation.resolve();
    if (this.animations.size === 0) {
      this.stopLoop();
      this.clear();
    }
  }

  private stopLoop(): void {
    if (this.frameRequest !== null) {
      cancelAnimationFrame(this.frameRequest);
      this.frameRequest = null;
    }
  }
}
