import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { page } from 'vitest/browser';
import { GameComponent } from '../game.component';
import { CUE_EFFECTS, CueEffect } from '../../../services/cue-effect-player.service';
import { DEBUG_FLASH_EFFECT } from './debug-flash.effect';
import { DEFAULT_EFFECT_MAX_MS } from './default-effects';
import { CUE_ANIMATIONS, cueAnimationsReady, drawCueAnimation, preloadCueAnimations } from './cue-animations';
import { PALETTES } from './cue-palette';
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
    type: target.permanent ? 'DAMAGE_TO_PERMANENT' : 'DAMAGE_TO_PLAYER', sourceCardId: 'shock',
    sourceName: 'Shock', sourceColors: ['RED'], targetPermanentId: target.permanent ?? null,
    targetPlayerId: target.player ?? null, targetCardId: null, amount: 3, counterType: null
  };
}

function stateWith(game: Game, ...cues: VisualCue[]): WebSocketMessage {
  return { ...game, type: MessageType.GAME_STATE, turnNumber: 2, visualCues: cues, newLogEntries: [],
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

/**
 * Mounts the game with `effects` registered, or with the app's own effects when `effects` is null;
 * the app's effects get their authored animations loaded first, as they are a moment into a game.
 */
async function mount(game: Game, effects: readonly CueEffect[] | null = [DEBUG_FLASH_EFFECT]): Promise<Mounted> {
  const messages = new Subject<WebSocketMessage>();
  const transport = {
    currentGame: game, currentUser: { userId: 'one', username: 'one' }, subgameDepth: 0,
    isConnected: () => true, getMessages: () => messages.asObservable(),
    onDisconnected: () => new Subject<void>().asObservable(), send: vi.fn()
  };
  TestBed.configureTestingModule({ providers: [
    { provide: WebsocketService, useValue: transport },
    { provide: Router, useValue: { navigate: vi.fn() } },
    ...(effects ? [{ provide: CUE_EFFECTS, useValue: effects }] : [])
  ] });
  const fixture = TestBed.createComponent(GameComponent);
  document.body.appendChild(fixture.nativeElement);
  fixture.detectChanges();
  // The board's zoom follows the area size, which a ResizeObserver reports a frame later.
  await vi.waitFor(() => expect(fixture.componentInstance.battlefieldAreaSize().width).toBeGreaterThan(0));
  fixture.detectChanges();
  await fixture.whenStable();
  if (!effects) expect(await preloadCueAnimations()).toBe(true);
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

/** What the overlay has drawn inside a rectangle: how many pixels, and their average color. */
function inkIn(rect: DOMRect): { pixels: number; r: number; g: number; b: number } {
  const c = canvas();
  const box = c.getBoundingClientRect();
  const ratio = c.width / box.width;
  const x = Math.max(0, Math.floor((rect.left - box.left) * ratio));
  const y = Math.max(0, Math.floor((rect.top - box.top) * ratio));
  const width = Math.max(1, Math.min(c.width - x, Math.floor(rect.width * ratio)));
  const height = Math.max(1, Math.min(c.height - y, Math.floor(rect.height * ratio)));
  const data = c.getContext('2d')!.getImageData(x, y, width, height).data;
  let pixels = 0, r = 0, g = 0, b = 0;
  for (let i = 0; i < data.length; i += 4) {
    if (data[i + 3] < 40) continue;
    pixels++;
    r += data[i];
    g += data[i + 1];
    b += data[i + 2];
  }
  return pixels === 0 ? { pixels, r: 0, g: 0, b: 0 } : { pixels, r: r / pixels, g: g / pixels, b: b / pixels };
}

function cueOf(type: VisualCue['type'], fields: Partial<VisualCue>): VisualCue {
  return {
    type, sourceCardId: null, sourceName: null, sourceColors: [], targetPermanentId: null, targetPlayerId: null,
    targetCardId: null, amount: null, counterType: null, ...fields
  };
}

describe('Default effects in the browser', () => {
  it('are registered by default and hold the state while they play', async () => {
    const game = board([creature('elf')], [creature('bear')]);
    const m = await mount(game, null);

    m.messages.next(stateWith(game, damage({ permanent: 'bear' })));
    await frames(3);

    expect(m.fixture.componentInstance.cuePlayback.playing()).toBe(true);
    expect(m.fixture.componentInstance.game()?.turnNumber).toBe(1);
  });

  it('burn a red damage target in warm colors and leave other cards alone', async () => {
    const game = board([creature('elf')], [creature('bear')]);
    const m = await mount(game, null);

    m.messages.next(stateWith(game, damage({ permanent: 'bear' })));

    await vi.waitFor(() => expect(inkIn(permanentRect('bear')).pixels).toBeGreaterThan(200), { timeout: 1000, interval: 16 });
    const ink = inkIn(permanentRect('bear'));
    expect(ink.r).toBeGreaterThan(ink.b);
    expect(inkIn(permanentRect('elf')).pixels).toBe(0);
  });

  it('cover a creature destroyed by a black spell in dark smoke', async () => {
    const game = board([creature('elf')], [creature('bear')]);
    const m = await mount(game, null);

    m.messages.next(stateWith(game, cueOf('DESTROYED', {
      sourceCardId: 'murder', sourceName: 'Murder', sourceColors: ['BLACK'], targetPermanentId: 'bear'
    })));

    await vi.waitFor(() => expect(inkIn(permanentRect('bear')).pixels).toBeGreaterThan(500), { timeout: 1000, interval: 16 });
    const ink = inkIn(permanentRect('bear'));
    expect((ink.r + ink.g + ink.b) / 3).toBeLessThan(100);
    expect(ink.b).toBeGreaterThan(ink.g);
  });

  it('play a combat step\'s damage on both creatures at once', async () => {
    const game = board([creature('elf')], [creature('bear')]);
    const m = await mount(game, null);
    const hit = (source: string, target: string) => cueOf('DAMAGE_TO_PERMANENT', {
      sourceCardId: 'card-' + source, sourceName: 'Bear ' + source, sourceColors: ['GREEN'], targetPermanentId: target, amount: 2
    });

    m.messages.next(stateWith(game, hit('elf', 'bear'), hit('bear', 'elf')));

    await vi.waitFor(() => {
      expect(inkIn(permanentRect('bear')).pixels).toBeGreaterThan(200);
      expect(inkIn(permanentRect('elf')).pixels).toBeGreaterThan(200);
    }, { timeout: 1000, interval: 16 });
  });

  it('show a spell cast with nothing to point at in the middle of the window', async () => {
    const game = board([creature('elf')], [creature('bear')]);
    const m = await mount(game, null);

    m.messages.next(stateWith(game, cueOf('SPELL_CAST', { sourceCardId: 'wrath', sourceColors: ['WHITE'] })));

    const middle = new DOMRect(window.innerWidth / 2 - 150, window.innerHeight / 2 - 150, 300, 300);
    await vi.waitFor(() => expect(inkIn(middle).pixels).toBeGreaterThan(100), { timeout: 1000, interval: 16 });
  });

  it('get through a whole Lightning Bolt, showpiece included, quickly, then apply the state', async () => {
    const game = board([creature('elf')], [creature('bear')]);
    const m = await mount(game, null);
    const bolt = { sourceCardId: 'bolt', sourceName: 'Lightning Bolt', sourceColors: ['RED'] };
    const started = performance.now();

    m.messages.next(stateWith(game, cueOf('SPELL_RESOLVED', bolt),
      cueOf('DAMAGE_TO_PERMANENT', { ...bolt, targetPermanentId: 'bear', amount: 3 }),
      cueOf('DIED', { targetPermanentId: 'bear' })));

    await vi.waitFor(() => expect(m.fixture.componentInstance.game()?.turnNumber).toBe(2), { timeout: 3000, interval: 16 });
    const elapsed = performance.now() - started;
    expect(elapsed).toBeGreaterThan(DEFAULT_EFFECT_MAX_MS);
    expect(elapsed).toBeLessThan(3 * DEFAULT_EFFECT_MAX_MS + 400);
  });
});

/** Every fill and stroke color slot a Lottie file uses. */
function slotsOf(node: unknown, slots = new Set<string>()): Set<string> {
  if (Array.isArray(node)) {
    node.forEach(item => slotsOf(item, slots));
  } else if (node && typeof node === 'object') {
    const item = node as Record<string, unknown>;
    if ((item['ty'] === 'fl' || item['ty'] === 'st') && typeof item['nm'] === 'string' && item['nm'].startsWith('@')) {
      slots.add(item['nm']);
    }
    Object.values(item).forEach(value => slotsOf(value, slots));
  }
  return slots;
}

describe('Authored animations in the browser', () => {
  it('are served for every name, and use only the known palette slots', async () => {
    for (const name of CUE_ANIMATIONS) {
      const response = await fetch(`cue-animations/${name}.json`);
      expect(response.ok, name).toBe(true);
      const data = await response.json();
      expect(data.w * data.h * (data.op - data.ip), name).toBeGreaterThan(0);
      expect(data.layers.length, name).toBeGreaterThan(0);
      expect([...slotsOf(data)].every(slot => ['@accent', '@c0', '@c1', '@c2'].includes(slot)), name).toBe(true);
    }
  });

  it('load lottie on demand and draw an animation in the look\'s colors', async () => {
    expect(await preloadCueAnimations()).toBe(true);
    expect(cueAnimationsReady()).toBe(true);
    const target = document.createElement('canvas');
    target.width = 400;
    target.height = 400;
    const draw = target.getContext('2d')!;

    expect(drawCueAnimation(draw, 'impact', PALETTES.fire, 0.3, { x: 200, y: 200, unit: 100 })).toBe(true);

    const data = draw.getImageData(0, 0, 400, 400).data;
    let pixels = 0, red = 0, blue = 0;
    for (let i = 0; i < data.length; i += 4) {
      if (data[i + 3] < 40) continue;
      pixels++;
      red += data[i];
      blue += data[i + 2];
    }
    expect(pixels).toBeGreaterThan(2000);
    expect(red).toBeGreaterThan(blue);
    expect(draw.getImageData(0, 0, 400, 400).data[3]).toBe(0);
  });

  it('are drawn on the overlay by the default effects', async () => {
    const drawImage = vi.spyOn(CanvasRenderingContext2D.prototype, 'drawImage');
    try {
      const game = board([creature('elf')], [creature('bear')]);
      const m = await mount(game, null);

      m.messages.next(stateWith(game, damage({ permanent: 'bear' })));

      await vi.waitFor(() => expect(drawImage.mock.calls.some(([image]) => image instanceof HTMLCanvasElement)).toBe(true),
        { timeout: 1000, interval: 16 });
    } finally {
      drawImage.mockRestore();
    }
  });
});

describe('Showpiece effects in the browser', () => {
  it('strike Lightning Bolt down from the top of the window onto its target, and shake the board', async () => {
    const game = board([creature('elf')], [creature('bear')]);
    const m = await mount(game, null);
    const bear = permanentRect('bear');

    m.messages.next(stateWith(game, cueOf('DAMAGE_TO_PERMANENT', {
      sourceCardId: 'bolt', sourceName: 'Lightning Bolt', sourceColors: ['RED'], targetPermanentId: 'bear', amount: 3
    })));

    const above = new DOMRect(bear.left - bear.width, 0, bear.width * 3, bear.top);
    await vi.waitFor(() => expect(inkIn(above).pixels).toBeGreaterThan(100), { timeout: 1000, interval: 16 });
    const layout = document.querySelector<HTMLElement>('.game-layout')!;
    await vi.waitFor(() => expect(layout.getAnimations().length).toBeGreaterThan(0), { timeout: 1000, interval: 16 });
    await vi.waitFor(() => expect(m.fixture.componentInstance.game()?.turnNumber).toBe(2), { timeout: 2000 });
    expect(layout.getAnimations()).toEqual([]);
  });

  it('gather Doom Blade\'s dark cloud above its target, then cover the target in dark smoke', async () => {
    const game = board([creature('elf')], [creature('bear')]);
    const m = await mount(game, null);
    const elf = permanentRect('elf');

    m.messages.next(stateWith(game, cueOf('DESTROYED', {
      sourceCardId: 'blade', sourceName: 'Doom Blade', sourceColors: ['BLACK'], targetPermanentId: 'elf'
    })));

    const above = new DOMRect(elf.left - elf.width / 2, elf.top - elf.width, elf.width * 2, elf.width * 0.8);
    await vi.waitFor(() => expect(inkIn(above).pixels).toBeGreaterThan(500), { timeout: 1000, interval: 16 });
    const cloud = inkIn(above);
    expect((cloud.r + cloud.g + cloud.b) / 3).toBeLessThan(110);
    await vi.waitFor(() => {
      const ink = inkIn(elf);
      expect(ink.pixels).toBeGreaterThan(500);
      expect((ink.r + ink.g + ink.b) / 3).toBeLessThan(110);
    }, { timeout: 2000, interval: 16 });
    expect(m.fixture.componentInstance.game()?.turnNumber).toBe(1);
  });
});
