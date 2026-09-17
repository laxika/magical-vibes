import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { GameComponent } from './game.component';
import { Game, MessageType, WebSocketMessage, WebsocketService } from '../../services/websocket.service';

describe('Game view transitions', () => {
  it('replaces the board and clears pending combat, choices, and surrender on each switch', () => {
    const messages = new Subject<WebSocketMessage>();
    const disconnected = new Subject<void>();
    const transport = {
      currentGame: { id: 'root', autoStopSteps: [] } as unknown as Game,
      subgameDepth: 0,
      isConnected: () => true,
      getMessages: () => messages.asObservable(),
      onDisconnected: () => disconnected.asObservable(),
      send: vi.fn()
    };
    TestBed.configureTestingModule({ providers: [
      { provide: WebsocketService, useValue: transport },
      { provide: Router, useValue: { navigate: vi.fn() } }
    ] });
    TestBed.overrideComponent(GameComponent, { set: { template: '', imports: [] } });
    const fixture = TestBed.createComponent(GameComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const resetChoices = vi.spyOn(component.choice, 'reset');

    for (const [id, depth] of [['child', 1], ['grandchild', 2], ['root', 0]] as const) {
      component.selectedAttackerIndices.set(new Set([3]));
      component.declaringAttackers.set(true);
      component.showSurrenderConfirm.set(true);
      component.stackTargetId.set('old-target');
      component.gameOverActive.set(true);
      transport.currentGame = { id, autoStopSteps: [] } as unknown as Game;
      transport.subgameDepth = depth;
      messages.next({ type: MessageType.ACTIVE_GAME_CHANGED });
      expect(component.game()?.id).toBe(id);
      expect(component.subgameDepth()).toBe(depth);
      expect(component.selectedAttackerIndices().size).toBe(0);
      expect(component.declaringAttackers()).toBe(false);
      expect(component.showSurrenderConfirm()).toBe(false);
      expect(component.stackTargetId()).toBeNull();
      expect(component.gameOverActive()).toBe(false);
    }
    expect(resetChoices).toHaveBeenCalledTimes(3);
    fixture.destroy();
  });
});
