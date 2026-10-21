import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { GameComponent } from './game.component';
import { CUE_PLAYER, CuePlayer } from '../../services/cue-playback.service';
import { Game, GameStatus, MessageType, VisualCue, WebSocketMessage, WebsocketService } from '../../services/websocket.service';

const BOLT: VisualCue = {
  type: 'DAMAGE_TO_PERMANENT', sourceCardId: 'bolt', sourceName: 'Lightning Bolt', sourceColors: ['RED'],
  targetPermanentId: 'bear', targetPlayerId: null, amount: 3
};

function stateMessage(turnNumber: number, visualCues: VisualCue[]): WebSocketMessage {
  return {
    type: MessageType.GAME_STATE, status: GameStatus.RUNNING, turnNumber, visualCues,
    battlefields: [[], []], stack: [], graveyards: [[], []], hand: [], manaPool: {}, autoStopSteps: [],
    playableCardIndices: [], newLogEntries: []
  } as unknown as WebSocketMessage;
}

function createGame(player?: CuePlayer) {
  const messages = new Subject<WebSocketMessage>();
  const transport = {
    currentGame: { id: 'root', status: GameStatus.RUNNING, turnNumber: 1, gameLog: [], autoStopSteps: [] } as unknown as Game,
    subgameDepth: 0,
    isConnected: () => true,
    getMessages: () => messages.asObservable(),
    onDisconnected: () => new Subject<void>().asObservable(),
    send: vi.fn()
  };
  TestBed.configureTestingModule({ providers: [
    { provide: WebsocketService, useValue: transport },
    { provide: Router, useValue: { navigate: vi.fn() } },
    ...(player ? [{ provide: CUE_PLAYER, useValue: player }] : [])
  ] });
  TestBed.overrideComponent(GameComponent, { set: { template: '', imports: [] } });
  const fixture = TestBed.createComponent(GameComponent);
  fixture.detectChanges();
  return { fixture, component: fixture.componentInstance, messages };
}

describe('Game cue playback', () => {
  afterEach(() => TestBed.resetTestingModule());

  it('applies a state with cues straight away when no effects are registered', () => {
    const { fixture, component, messages } = createGame();

    messages.next(stateMessage(2, [BOLT]));

    expect(component.game()?.turnNumber).toBe(2);
    expect(component.cuePlayback.playing()).toBe(false);
    fixture.destroy();
  });

  it('holds the state while its cues play, and a key press skips to it', async () => {
    const play = vi.fn(() => new Promise<void>(() => {}));
    const { fixture, component, messages } = createGame({ canPlay: () => true, play });

    messages.next(stateMessage(2, [BOLT]));
    await Promise.resolve();

    expect(play).toHaveBeenCalledWith(BOLT, expect.objectContaining({ speed: 1 }));
    expect(component.game()?.turnNumber).toBe(1);
    expect(component.cuePlayback.playing()).toBe(true);

    component.onKeydown(new KeyboardEvent('keydown', { key: ' ' }));

    expect(component.game()?.turnNumber).toBe(2);
    expect(component.cuePlayback.playing()).toBe(false);
    fixture.destroy();
  });
});
