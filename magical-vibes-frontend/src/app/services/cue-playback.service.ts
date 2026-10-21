import { Injectable, InjectionToken, inject, signal } from '@angular/core';
import { GameStateNotification, MessageType, VisualCue, WebSocketMessage } from './websocket.service';
import { CueEffectPlayer } from './cue-effect-player.service';

/** What a cue player gets besides the cue: when to stop, and how fast to go. */
export interface CuePlaybackContext {
  /** Aborted when the cue is skipped or runs out of time; the player should stop drawing. */
  signal: AbortSignal;
  /** Playback speed multiplier (1 = normal, 2 = twice as fast). */
  speed: number;
}

/**
 * Draws visual cues on the board. The queue plays the cues of a state message one at a time, in
 * order, on the board as it currently is, and applies the state when they are done.
 */
export interface CuePlayer {
  /** Whether this player has an effect for the cue. Cues it can't play are left out. */
  canPlay(cue: VisualCue): boolean;
  /** Plays one cue; resolves when the effect is over (or aborted). */
  play(cue: VisualCue, context: CuePlaybackContext): Promise<void>;
}

/** Plays nothing, so every state is applied as soon as it arrives. */
export const NOOP_CUE_PLAYER: CuePlayer = {
  canPlay: () => false,
  play: () => Promise.resolve()
};

/** The player the queue uses: the effect registry, which draws on the effects overlay. */
export const CUE_PLAYER = new InjectionToken<CuePlayer>('CUE_PLAYER', {
  providedIn: 'root',
  factory: () => inject(CueEffectPlayer)
});

/** The longest a single state message's cues may hold the board back, at normal speed. */
export const MAX_CUE_PLAYBACK_MS = 3000;

/**
 * Messages that ask this player to decide something, answer their own request, or replace the
 * whole game view. They are never held back: everything queued before them is applied at once,
 * so the board the player decides on is the current one.
 */
const DECISION_MESSAGES: ReadonlySet<string> = new Set([
  MessageType.AVAILABLE_ATTACKERS,
  MessageType.AVAILABLE_BLOCKERS,
  MessageType.INTERACTION_PROMPT,
  MessageType.COMBAT_DAMAGE_ASSIGNMENT,
  MessageType.SELECT_CARDS_TO_BOTTOM,
  MessageType.MULLIGAN_RESOLVED,
  MessageType.REVEAL_HAND,
  MessageType.REVEAL_LIBRARY_TOP,
  MessageType.REVEAL_PERMANENT,
  MessageType.VALID_TARGETS_RESPONSE,
  MessageType.OPPONENT_JOINED
]);

/**
 * Sits in front of the game component's message handling. A game state that carries cues the
 * player can draw is held back while they play, and every message behind it waits its turn;
 * anything else is applied straight away, exactly as without the queue. Decisions flush the
 * queue, an active-game switch drops it, and no message's cues may take longer than
 * {@link MAX_CUE_PLAYBACK_MS} (scaled by speed). Cues are cosmetic: skipping them never changes
 * which messages are applied or in what order.
 */
@Injectable()
export class CuePlaybackService {
  private readonly player = inject(CUE_PLAYER);
  private apply: (message: WebSocketMessage) => void = () => {};
  private queue: WebSocketMessage[] = [];
  private draining = false;
  // Bumped whenever the queue is flushed or dropped, so a drain that was waiting on cues stops.
  private generation = 0;
  private abortPlayback: AbortController | null = null;

  /** True while cues are being drawn and the state they lead to is held back. */
  readonly playing = signal(false);
  /** Playback speed multiplier. */
  readonly speed = signal(1);

  /** Sets the handler that applies a message to the game view. */
  init(apply: (message: WebSocketMessage) => void): void {
    this.apply = apply;
  }

  /** Handles an incoming game message: applies it now, or queues it behind playing cues. */
  push(message: WebSocketMessage): void {
    if (message.type === MessageType.ACTIVE_GAME_CHANGED) {
      // The queued messages belong to the game being left; the switch replaces its view.
      this.clear();
      this.apply(message);
      return;
    }
    if (DECISION_MESSAGES.has(message.type)) {
      this.flush();
      this.apply(message);
      return;
    }
    if (this.queue.length === 0 && this.playableCues(message).length === 0) {
      this.apply(message);
      return;
    }
    this.queue.push(message);
    if (!this.draining) {
      void this.drain();
    }
  }

  /** Stops the playing cues and applies everything queued, in order. */
  flush(): void {
    this.stopPlayback();
    const pending = this.queue;
    this.queue = [];
    pending.forEach(message => this.apply(message));
  }

  /** The player's skip control: same as {@link flush}. */
  skip(): void {
    this.flush();
  }

  /** Stops the playing cues and drops everything queued without applying it. */
  clear(): void {
    this.stopPlayback();
    this.queue = [];
  }

  private stopPlayback(): void {
    this.generation++;
    this.abortPlayback?.abort();
    this.abortPlayback = null;
    this.draining = false;
    this.playing.set(false);
  }

  private playableCues(message: WebSocketMessage): VisualCue[] {
    if (message.type !== MessageType.GAME_STATE) return [];
    const cues = (message as GameStateNotification).visualCues ?? [];
    return cues.filter(cue => this.player.canPlay(cue));
  }

  private async drain(): Promise<void> {
    const generation = this.generation;
    this.draining = true;
    while (this.queue.length > 0) {
      const message = this.queue[0];
      const cues = this.playableCues(message);
      if (cues.length > 0) {
        this.playing.set(true);
        await this.play(cues);
        if (generation !== this.generation) return;
      }
      this.queue.shift();
      this.apply(message);
    }
    this.draining = false;
    this.playing.set(false);
  }

  /** Plays the cues in order; resolves when they finish, time runs out, or playback is stopped. */
  private async play(cues: VisualCue[]): Promise<void> {
    const abort = new AbortController();
    this.abortPlayback = abort;
    const speed = Math.max(this.speed(), 0.01);
    let timer: ReturnType<typeof setTimeout> | undefined;
    const timeLimit = new Promise<void>(resolve => {
      timer = setTimeout(resolve, MAX_CUE_PLAYBACK_MS / speed);
    });
    const stopped = new Promise<void>(resolve => abort.signal.addEventListener('abort', () => resolve()));
    const sequence = (async () => {
      for (const cue of cues) {
        if (abort.signal.aborted) return;
        try {
          await this.player.play(cue, { signal: abort.signal, speed });
        } catch (error) {
          console.error('Visual cue failed', cue, error);
        }
      }
    })();
    await Promise.race([sequence, timeLimit, stopped]);
    clearTimeout(timer);
    abort.abort();
    if (this.abortPlayback === abort) {
      this.abortPlayback = null;
    }
  }
}
