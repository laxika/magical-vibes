import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { MessageType, WebsocketService } from './websocket.service';

class Socket {
  static OPEN = 1;
  static latest: Socket;
  readyState = 1;
  onopen: (() => void) | null = null;
  onmessage: ((event: { data: string }) => void) | null = null;
  onclose: (() => void) | null = null;
  onerror: (() => void) | null = null;
  sent: object[] = [];
  constructor() { Socket.latest = this; }
  send(text: string): void { this.sent.push(JSON.parse(text)); }
  close(): void { this.onclose?.(); }
  receive(message: object): void { this.onmessage?.({ data: JSON.stringify(message) }); }
}

describe('Subgame transport context', () => {
  beforeEach(() => vi.stubGlobal('WebSocket', Socket));
  afterEach(() => vi.unstubAllGlobals());

  function connect(): [WebsocketService, Socket] {
    const service = new WebsocketService();
    service.login('player', 'password').subscribe();
    const socket = Socket.latest;
    socket.onopen?.();
    socket.receive({ type: MessageType.LOGIN_SUCCESS, userId: 'player', username: 'player' });
    return [service, socket];
  }

  it('installs the replacement board and epoch before notifying game subscribers', () => {
    const [service, socket] = connect();
    const context = { sessionId: 'root', activeGameId: 'child', activationEpoch: 1 };
    let observed: unknown;
    service.getMessages().subscribe(() => { observed = service.currentGame; });
    socket.receive({ type: MessageType.ACTIVE_GAME_CHANGED, context, depth: 1, game: { id: 'child' } });
    expect(observed).toEqual({ id: 'child' });
    expect(service.subgameDepth).toBe(1);
    service.send({ type: MessageType.PASS_PRIORITY });
    expect(socket.sent.at(-1)).toEqual({ type: MessageType.PASS_PRIORITY, gameContext: context });
  });

  it('uses a fresh epoch when returning to a parent with the same game ID', () => {
    const [service, socket] = connect();
    socket.receive({ type: MessageType.ACTIVE_GAME_CHANGED,
      context: { sessionId: 'root', activeGameId: 'child', activationEpoch: 1 }, depth: 1, game: { id: 'child' } });
    socket.receive({ type: MessageType.ACTIVE_GAME_CHANGED,
      context: { sessionId: 'root', activeGameId: 'root', activationEpoch: 2 }, depth: 0, game: { id: 'root' } });
    service.send({ type: MessageType.SURRENDER });
    expect(socket.sent.at(-1)).toMatchObject({ gameContext: { activeGameId: 'root', activationEpoch: 2 } });
    expect(service.subgameDepth).toBe(0);
  });

  it('clears the previous session context when joining another game or disconnecting', () => {
    const [service, socket] = connect();
    socket.receive({ type: MessageType.ACTIVE_GAME_CHANGED,
      context: { sessionId: 'root', activeGameId: 'child', activationEpoch: 1 }, depth: 1, game: { id: 'child' } });
    socket.receive({ type: MessageType.GAME_JOINED, game: { id: 'another' } });
    expect(service.gameContext).toBeNull();
    expect(service.subgameDepth).toBe(0);
    service.disconnect();
    expect(service.currentGame).toBeNull();
  });
});
