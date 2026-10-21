import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { page } from 'vitest/browser';
import { GameComponent } from '../game.component';
import { CUE_EFFECTS } from '../../../services/cue-effect-player.service';
import { DEBUG_FLASH_EFFECT } from './debug-flash.effect';
import {
  Card, Game, GameStatus, MessageType, Permanent, TurnStep, VisualCue, WebSocketMessage, WebsocketService
} from '../../../services/websocket.service';

const DESKTOP = { width: 1280, height: 900 };

function card(id: string, name: string): Card {
  return {
    id, name, type: 'CREATURE', additionalTypes: [], supertypes: [], subtypes: [], cardText: null,
    manaCost: '{1}{G}', power: 2, toughness: 2, keywords: [], hasTapAbility: false, setCode: null,
    collectorNumber: null, color: 'GREEN', colors: ['GREEN'], needsTarget: false, needsSpellTarget: false,
    activatedAbilities: [], loyalty: null, hasConvoke: false, hasPhyrexianMana: false, phyrexianManaCount: 0,
    token: false, watermark: null, hasAlternateCastingCost: false, alternateCostLifePayment: 0,
    alternateCostSacrificeCount: 0, alternateCostTapCount: 0, alternateCostReturnCount: 0,
    alternateCostManaCost: null, alternateCostExileHandCount: 0, alternateCostExileHandLabel: null,
    additionalBeholdSubtype: null, additionalBeholdCount: 0, additionalBeholdFlashbackOnly: false,
    additionalBeholdChosenCreatureType: false, graveyardActivatedAbilities: [], transformable: false,
    kickerCost: null, kickerRequiresTap: false, kickerRequiresReturn: false, buybackCost: null,
    modalChoicesRequired: 0, modalChoicesMax: 0, modalOptional: false, modalOptions: null,
    exileCastCounterCost: 0, prepareSpell: null
  };
}

function creature(id: string): Permanent {
  return {
    id, card: card('card-' + id, 'Bear ' + id), tapped: false, attacking: false, blocking: false,
    blockingTargets: [], summoningSick: false, powerModifier: 0, toughnessModifier: 0, grantedKeywords: [],
    removedKeywords: [], effectivePower: 2, effectiveToughness: 2, chosenColor: null, chosenName: null,
    regenerationShield: 0, attachedTo: null, cantBeBlocked: false, animatedCreature: false, counters: {},
    attackTargetId: null, markedDamage: 0, transformed: false, prepared: false
  };
}

function board(mine: Permanent[], theirs: Permanent[]): Game {
  return {
    id: 'root', gameName: 'Effects', status: GameStatus.RUNNING,
    playerIds: ['one', 'two'], playerNames: ['One', 'Two'], gameLog: [],
    currentStep: TurnStep.PRECOMBAT_MAIN, activePlayerId: 'one', priorityPlayerId: 'one',
    turnNumber: 1, dayNight: 'NEITHER', hand: [], opponentHand: [], mulliganCount: 0,
    deckSizes: [30, 30], handSizes: [7, 7], battlefields: [mine, theirs], manaPool: {},
    autoStopSteps: [], lifeTotals: [20, 20], poisonCounters: [0, 0], energyCounters: [0, 0],
    speeds: [0, 0], stack: [], graveyards: [[], []], revealedLibraryTopCards: [[], []],
    monarchPlayerId: null
  };
}

function damage(target: { permanent?: string; player?: string }): VisualCue {
  return {
    type: target.permanent ? 'DAMAGE_TO_PERMANENT' : 'DAMAGE_TO_PLAYER', sourceCardId: 'bolt',
    sourceName: 'Lightning Bolt', sourceColors: ['RED'], targetPermanentId: target.permanent ?? null,
    targetPlayerId: target.player ?? null, targetCardId: null, amount: 3, counterType: null
  };
}

function stateWith(game: Game, cue: VisualCue): WebSocketMessage {
  return { ...game, type: MessageType.GAME_STATE, turnNumber: 2, visualCues: [cue], newLogEntries: [],
    playableCardIndices: [] } as unknown as WebSocketMessage;
}

interface Mounted {
  fixture: ComponentFixture<GameComponent>;
  messages: Subject<WebSocketMessage>;
}

let mounted: Mounted | null = null;

afterEach(async () => {
  mounted?.fixture.destroy();
  mounted?.fixture.nativeElement.remove();
  mounted = null;
  TestBed.resetTestingModule();
  delete (window as { devicePixelRatio?: number }).devicePixelRatio;
  await page.viewport(DESKTOP.width, DESKTOP.height);
});

async function mount(game: Game, effects = [DEBUG_FLASH_EFFECT]): Promise<Mounted> {
  const messages = new Subject<WebSocketMessage>();
  const transport = {
    currentGame: game, currentUser: { userId: 'one', username: 'one' }, subgameDepth: 0,
    isConnected: () => true, getMessages: () => messages.asObservable(),
    onDisconnected: () => new Subject<void>().asObservable(), send: vi.fn()
  };
  TestBed.configureTestingModule({ providers: [
    { provide: WebsocketService, useValue: transport },
    { provide: Router, useValue: { navigate: vi.fn() } },
    { provide: CUE_EFFECTS, useValue: effects }
  ] });
  const fixture = TestBed.createComponent(GameComponent);
  document.body.appendChild(fixture.nativeElement);
  fixture.detectChanges();
  // The board's zoom follows the area size, which a ResizeObserver reports a frame later.
  await vi.waitFor(() => expect(fixture.componentInstance.battlefieldAreaSize().width).toBeGreaterThan(0));
  fixture.detectChanges();
  await fixture.whenStable();
  await frames(1);
  mounted = { fixture, messages };
  return mounted;
}

function frames(count: number): Promise<void> {
  return new Promise(resolve => {
    const step = (left: number) => left === 0 ? resolve() : requestAnimationFrame(() => step(left - 1));
    step(count);
  });
}

function canvas(): HTMLCanvasElement {
  return document.querySelector('app-cue-overlay canvas') as HTMLCanvasElement;
}

/** Alpha of the overlay's pixel at a viewport point. */
function overlayAlpha(x: number, y: number): number {
  const c = canvas();
  const box = c.getBoundingClientRect();
  const ratio = c.width / box.width;
  const px = Math.floor((x - box.left) * ratio);
  const py = Math.floor((y - box.top) * ratio);
  return c.getContext('2d')!.getImageData(px, py, 1, 1).data[3];
}

function permanentRect(id: string): DOMRect {
  const display = [...document.querySelectorAll<HTMLElement>('app-card-display')]
    .find(el => el.textContent?.includes('Bear ' + id));
  return display!.getBoundingClientRect();
}

function centre(rect: DOMRect): [number, number] {
  return [rect.left + rect.width / 2, rect.top + rect.height / 2];
}

/** Plays the cue and waits until the flash has been drawn a few times. */
async function playFlash(m: Mounted, game: Game, cue: VisualCue): Promise<void> {
  m.messages.next(stateWith(game, cue));
  await frames(3);
  expect(m.fixture.componentInstance.cuePlayback.playing()).toBe(true);
}

function expectFlashOn(rect: DOMRect, others: DOMRect[]): void {
  expect(overlayAlpha(...centre(rect))).toBeGreaterThan(0);
  expect(overlayAlpha(rect.left + 6, rect.top + 6)).toBeGreaterThan(0);
  expect(overlayAlpha(rect.right - 6, rect.bottom - 6)).toBeGreaterThan(0);
  for (const other of others) {
    expect(overlayAlpha(...centre(other))).toBe(0);
  }
}

describe('Effects overlay in the browser', () => {
  it('flashes the targeted permanent on desktop, then applies the state and stops drawing', async () => {
    const game = board([creature('elf')], [creature('bear'), creature('wolf')]);
    const m = await mount(game);

    await playFlash(m, game, damage({ permanent: 'bear' }));

    expectFlashOn(permanentRect('bear'), [permanentRect('elf'), permanentRect('wolf')]);
    expect(m.fixture.componentInstance.game()?.turnNumber).toBe(1);

    await vi.waitFor(() => expect(m.fixture.componentInstance.game()?.turnNumber).toBe(2), { timeout: 2000 });
    await frames(2);
    const overlay = m.fixture.debugElement.query(el => el.name === 'app-cue-overlay').componentInstance;
    expect(overlay.animating).toBe(false);
    expect(overlayAlpha(...centre(permanentRect('bear')))).toBe(0);
  });

  it('never takes input', async () => {
    const game = board([creature('elf')], []);
    await mount(game);

    expect(getComputedStyle(document.querySelector('app-cue-overlay')!).pointerEvents).toBe('none');
  });

  it('lands on the right card when a crowded board is zoomed out', async () => {
    const mine = Array.from({ length: 16 }, (_, i) => creature('c' + i));
    const game = board(mine, [creature('bear')]);
    const m = await mount(game);
    expect(m.fixture.componentInstance.myBattlefieldZoom).toBeLessThan(0.8);

    await playFlash(m, game, damage({ permanent: 'c9' }));

    expectFlashOn(permanentRect('c9'), [permanentRect('c8'), permanentRect('c10'), permanentRect('bear')]);
  });

  it('lands on the right card in the phone layout', async () => {
    await page.viewport(390, 844);
    expect(matchMedia('(max-width: 700px)').matches).toBe(true);
    const game = board([creature('elf'), creature('ox')], [creature('bear')]);
    const m = await mount(game);

    await playFlash(m, game, damage({ permanent: 'ox' }));

    expectFlashOn(permanentRect('ox'), [permanentRect('elf'), permanentRect('bear')]);
  });

  it('draws sharp on a high-DPI screen and still lands on the target', async () => {
    Object.defineProperty(window, 'devicePixelRatio', { value: 2, configurable: true });
    const game = board([creature('elf')], [creature('bear')]);
    const m = await mount(game);

    await playFlash(m, game, damage({ permanent: 'bear' }));

    const box = canvas().getBoundingClientRect();
    expect(canvas().width).toBe(Math.round(box.width * 2));
    expect(canvas().height).toBe(Math.round(box.height * 2));
    expectFlashOn(permanentRect('bear'), [permanentRect('elf')]);
  });

  it('flashes a player panel for damage to a player', async () => {
    const game = board([creature('elf')], [creature('bear')]);
    const m = await mount(game);

    await playFlash(m, game, damage({ player: 'two' }));

    const badges = [...document.querySelectorAll<HTMLElement>('.player-badge')];
    const opponent = badges.find(b => b.textContent?.includes('Two'))!.getBoundingClientRect();
    const me = badges.find(b => b.textContent?.includes('One'))!.getBoundingClientRect();
    expect(overlayAlpha(...centre(opponent))).toBeGreaterThan(0);
    expect(overlayAlpha(...centre(me))).toBe(0);
    expect(overlayAlpha(...centre(permanentRect('bear')))).toBe(0);
  });

  it('applies the state at once when no effect is registered', async () => {
    const game = board([creature('elf')], [creature('bear')]);
    const m = await mount(game, []);

    m.messages.next(stateWith(game, damage({ permanent: 'bear' })));

    expect(m.fixture.componentInstance.game()?.turnNumber).toBe(2);
    expect(m.fixture.componentInstance.cuePlayback.playing()).toBe(false);
  });

  it('skips a target that is not on screen', async () => {
    const game = board([creature('elf')], [creature('bear')]);
    const m = await mount(game);

    m.messages.next(stateWith(game, damage({ permanent: 'gone' })));

    expect(m.fixture.componentInstance.game()?.turnNumber).toBe(2);
    expect(m.fixture.componentInstance.cuePlayback.playing()).toBe(false);
  });
});
