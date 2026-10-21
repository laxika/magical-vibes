import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { CUE_PLAYER, CuePlaybackContext, CuePlaybackService, CuePlayer, MAX_CUE_PLAYBACK_MS, cueGroups } from './cue-playback.service';
import { MessageType, VisualCue, VisualCueType, WebSocketMessage } from './websocket.service';

function cue(type: VisualCueType, targetPermanentId: string | null = null): VisualCue {
  return { type, sourceCardId: null, sourceName: null, sourceColors: [], targetPermanentId, targetPlayerId: null, targetCardId: null, amount: null,
    counterType: null };
}

function state(id: string, visualCues: VisualCue[] = []): WebSocketMessage {
  return { type: MessageType.GAME_STATE, id, visualCues } as unknown as WebSocketMessage;
}

function message(type: MessageType, id: string): WebSocketMessage {
  return { type, id } as unknown as WebSocketMessage;
}

/** Plays every cue except LIFE_GAINED; each cue finishes only when the test says so. */
class FakeCuePlayer implements CuePlayer {
  readonly started: { cue: VisualCue; context: CuePlaybackContext; finish: () => void }[] = [];

  canPlay(cue: VisualCue): boolean {
    return cue.type !== 'LIFE_GAINED';
  }

  play(cue: VisualCue, context: CuePlaybackContext): Promise<void> {
    return new Promise(resolve => this.started.push({ cue, context, finish: resolve }));
  }

  get playedTypes(): VisualCueType[] {
    return this.started.map(s => s.cue.type);
  }
}

describe('CuePlaybackService', () => {
  let player: FakeCuePlayer;
  let playback: CuePlaybackService;
  let applied: string[];

  /** Lets pending promise callbacks (the queue's awaits) run. */
  const settle = () => vi.advanceTimersByTimeAsync(0);

  beforeEach(() => {
    vi.useFakeTimers();
    player = new FakeCuePlayer();
    TestBed.configureTestingModule({ providers: [CuePlaybackService, { provide: CUE_PLAYER, useValue: player }] });
    playback = TestBed.inject(CuePlaybackService);
    applied = [];
    playback.init(m => applied.push((m as unknown as { id: string }).id));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('applies a message without playable cues straight away', () => {
    playback.push(state('plain'));
    playback.push(state('only-unplayable', [cue('LIFE_GAINED')]));
    playback.push(message(MessageType.GAME_OVER, 'over'));

    expect(applied).toEqual(['plain', 'only-unplayable', 'over']);
    expect(player.started).toHaveLength(0);
    expect(playback.playing()).toBe(false);
  });

  it('applies everything straight away with the default player, which plays nothing', () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ providers: [CuePlaybackService] });
    const defaultPlayback = TestBed.inject(CuePlaybackService);
    const seen: string[] = [];
    defaultPlayback.init(m => seen.push((m as unknown as { id: string }).id));

    defaultPlayback.push(state('bolt', [cue('DAMAGE_TO_PERMANENT', 'bear'), cue('DIED', 'bear')]));

    expect(seen).toEqual(['bolt']);
    expect(defaultPlayback.playing()).toBe(false);
  });

  it('plays a state\'s cues in order before applying it, and later messages wait their turn', async () => {
    playback.push(state('bolt', [cue('DAMAGE_TO_PERMANENT', 'bear'), cue('LIFE_GAINED'), cue('DIED', 'bear')]));
    playback.push(state('after'));
    playback.push(message(MessageType.GAME_OVER, 'over'));
    await settle();

    expect(applied).toEqual([]);
    expect(playback.playing()).toBe(true);
    expect(player.playedTypes).toEqual(['DAMAGE_TO_PERMANENT']);

    player.started[0].finish();
    await settle();
    expect(player.playedTypes).toEqual(['DAMAGE_TO_PERMANENT', 'DIED']);
    expect(applied).toEqual([]);

    player.started[1].finish();
    await settle();
    expect(applied).toEqual(['bolt', 'after', 'over']);
    expect(playback.playing()).toBe(false);
  });

  it('plays cues of one kind in a row together, and waits for all of them before the next', async () => {
    playback.push(state('combat', [
      cue('DAMAGE_TO_PERMANENT', 'bear'), cue('DAMAGE_TO_PLAYER'), cue('DAMAGE_TO_PERMANENT', 'elf'),
      cue('DIED', 'bear'), cue('DESTROYED', 'elf'), cue('SPELL_RESOLVED'), cue('SPELL_RESOLVED')
    ]));
    await settle();
    expect(player.playedTypes).toEqual(['DAMAGE_TO_PERMANENT', 'DAMAGE_TO_PLAYER', 'DAMAGE_TO_PERMANENT']);

    player.started[0].finish();
    player.started[2].finish();
    await settle();
    expect(player.started).toHaveLength(3);

    player.started[1].finish();
    await settle();
    expect(player.playedTypes.slice(3)).toEqual(['DIED', 'DESTROYED']);

    player.started[3].finish();
    player.started[4].finish();
    await settle();
    expect(player.playedTypes.slice(5)).toEqual(['SPELL_RESOLVED']);

    player.started[5].finish();
    await settle();
    expect(player.playedTypes.slice(5)).toEqual(['SPELL_RESOLVED', 'SPELL_RESOLVED']);
    player.started[6].finish();
    await settle();
    expect(applied).toEqual(['combat']);
  });

  it('groups only consecutive cues of the same kind', () => {
    const groups = cueGroups([
      cue('DAMAGE_TO_PERMANENT'), cue('LIFE_GAINED'), cue('LIFE_LOST'), cue('DAMAGE_TO_PLAYER'),
      cue('COUNTERS_ADDED'), cue('COUNTERS_ADDED'), cue('SPELL_CAST'), cue('SPELL_CAST'), cue('EXILED'), cue('RETURNED_TO_HAND')
    ]);

    expect(groups.map(group => group.map(c => c.type))).toEqual([
      ['DAMAGE_TO_PERMANENT'], ['LIFE_GAINED', 'LIFE_LOST'], ['DAMAGE_TO_PLAYER'], ['COUNTERS_ADDED', 'COUNTERS_ADDED'],
      ['SPELL_CAST'], ['SPELL_CAST'], ['EXILED', 'RETURNED_TO_HAND']
    ]);
  });

  it('plays queued states\' cues one message after another', async () => {
    playback.push(state('first', [cue('DESTROYED', 'a')]));
    playback.push(state('second', [cue('EXILED', 'b')]));
    await settle();
    expect(player.playedTypes).toEqual(['DESTROYED']);

    player.started[0].finish();
    await settle();
    expect(applied).toEqual(['first']);
    expect(player.playedTypes).toEqual(['DESTROYED', 'EXILED']);

    player.started[1].finish();
    await settle();
    expect(applied).toEqual(['first', 'second']);
  });

  it('flushes the queue in order when the player must make a decision', async () => {
    playback.push(state('bolt', [cue('DAMAGE_TO_PERMANENT', 'bear'), cue('DIED', 'bear')]));
    playback.push(state('after', [cue('DESTROYED', 'other')]));
    await settle();

    playback.push(message(MessageType.INTERACTION_PROMPT, 'prompt'));

    expect(applied).toEqual(['bolt', 'after', 'prompt']);
    expect(playback.playing()).toBe(false);
    expect(player.started[0].context.signal.aborted).toBe(true);

    // The interrupted cue finishing late changes nothing.
    player.started[0].finish();
    await settle();
    expect(player.playedTypes).toEqual(['DAMAGE_TO_PERMANENT']);
    expect(applied).toEqual(['bolt', 'after', 'prompt']);
  });

  it.each([
    MessageType.AVAILABLE_ATTACKERS,
    MessageType.AVAILABLE_BLOCKERS,
    MessageType.COMBAT_DAMAGE_ASSIGNMENT,
    MessageType.SELECT_CARDS_TO_BOTTOM,
    MessageType.REVEAL_HAND,
    MessageType.VALID_TARGETS_RESPONSE
  ])('treats %s as a decision', async type => {
    playback.push(state('bolt', [cue('DAMAGE_TO_PLAYER')]));
    await settle();

    playback.push(message(type, 'decision'));

    expect(applied).toEqual(['bolt', 'decision']);
  });

  it('applies a state once its cues reach the time limit', async () => {
    playback.push(state('slow', [cue('DESTROYED', 'a'), cue('DAMAGE_TO_PERMANENT', 'b')]));
    playback.push(state('after'));
    await settle();

    await vi.advanceTimersByTimeAsync(MAX_CUE_PLAYBACK_MS - 1);
    expect(applied).toEqual([]);

    await vi.advanceTimersByTimeAsync(1);
    expect(applied).toEqual(['slow', 'after']);
    expect(player.started[0].context.signal.aborted).toBe(true);
    expect(player.playedTypes).toEqual(['DESTROYED']);
  });

  it('scales the time limit with the speed and passes the speed to the player', async () => {
    playback.speed.set(2);
    playback.push(state('fast', [cue('DESTROYED', 'a')]));
    await settle();
    expect(player.started[0].context.speed).toBe(2);

    await vi.advanceTimersByTimeAsync(MAX_CUE_PLAYBACK_MS / 2);
    expect(applied).toEqual(['fast']);
  });

  it('applies everything queued when the player skips', async () => {
    playback.push(state('bolt', [cue('DAMAGE_TO_PERMANENT', 'bear')]));
    playback.push(state('after', [cue('DIED', 'bear')]));
    await settle();

    playback.skip();

    expect(applied).toEqual(['bolt', 'after']);
    expect(playback.playing()).toBe(false);
    await vi.advanceTimersByTimeAsync(MAX_CUE_PLAYBACK_MS);
    expect(applied).toEqual(['bolt', 'after']);
  });

  it('drops the queue on an active-game switch and applies the switch', async () => {
    playback.push(state('old-game', [cue('DESTROYED', 'a')]));
    playback.push(state('old-game-later'));
    await settle();

    playback.push(message(MessageType.ACTIVE_GAME_CHANGED, 'switch'));
    playback.push(state('new-game'));

    expect(applied).toEqual(['switch', 'new-game']);
    expect(player.started[0].context.signal.aborted).toBe(true);
    await vi.advanceTimersByTimeAsync(MAX_CUE_PLAYBACK_MS);
    expect(applied).toEqual(['switch', 'new-game']);
  });

  it('keeps going when a cue fails', async () => {
    const error = vi.spyOn(console, 'error').mockImplementation(() => {});
    player.play = vi.fn().mockRejectedValueOnce(new Error('broken')).mockResolvedValue(undefined);
    playback.push(state('broken', [cue('DESTROYED', 'a'), cue('DAMAGE_TO_PERMANENT', 'b')]));
    await settle();

    expect(player.play).toHaveBeenCalledTimes(2);
    expect(applied).toEqual(['broken']);
    error.mockRestore();
  });
});
