import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { GameComponent } from './game.component';
import { Game, GameStatus, MessageType, TurnStep, WebSocketMessage, WebsocketService } from '../../services/websocket.service';

afterEach(() => TestBed.resetTestingModule());

function board(id: string): Game {
  return {
    id, gameName: 'Shahrazad', status: GameStatus.RUNNING,
    playerIds: ['one', 'two'], playerNames: ['One', 'Two'], gameLog: [],
    currentStep: TurnStep.PRECOMBAT_MAIN, activePlayerId: 'one', priorityPlayerId: 'one',
    turnNumber: 1, dayNight: 'NEITHER', hand: [], opponentHand: [], mulliganCount: 0,
    deckSizes: [30, 30], handSizes: [7, 7], battlefields: [[], []], manaPool: {},
    autoStopSteps: [], lifeTotals: [20, 20], poisonCounters: [0, 0], energyCounters: [0, 0],
    speeds: [0, 0], stack: [], graveyards: [[], []], revealedLibraryTopCards: [[], []],
    monarchPlayerId: null
  };
}

describe('Subgame board in the browser', () => {
  for (const userId of ['one', 'two']) {
    it('shows depth and current-game concession through five levels for ' + userId, async () => {
      const messages = new Subject<WebSocketMessage>();
      const disconnected = new Subject<void>();
      const transport = {
        currentGame: board('root'), currentUser: { userId, username: userId }, subgameDepth: 0,
        isConnected: () => true, getMessages: () => messages.asObservable(),
        onDisconnected: () => disconnected.asObservable(), send: vi.fn()
      };
      TestBed.configureTestingModule({ providers: [
        { provide: WebsocketService, useValue: transport },
        { provide: Router, useValue: { navigate: vi.fn() } }
      ] });
      const fixture = TestBed.createComponent(GameComponent);
      document.body.appendChild(fixture.nativeElement);
      fixture.detectChanges();
      for (const depth of [1, 2, 3, 4, 5, 4, 3, 2, 1, 0]) {
        transport.currentGame = board(depth === 0 ? 'root' : 'child-' + depth);
        transport.subgameDepth = depth;
        messages.next({ type: MessageType.ACTIVE_GAME_CHANGED });
        fixture.detectChanges();
        await fixture.whenStable();
        expect(fixture.nativeElement.querySelector('.game-context').textContent)
          .toContain(depth ? 'Subgame depth ' + depth : 'Main game');
        fixture.componentInstance.showSurrenderConfirm.set(true);
        fixture.detectChanges();
        expect(fixture.nativeElement.querySelector('.winner-text').textContent)
          .toContain(depth ? 'Concede this subgame' : 'Do you want to surrender');
      }
      fixture.destroy();
    });
  }
});
