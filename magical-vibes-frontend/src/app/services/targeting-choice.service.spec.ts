import { signal, WritableSignal } from '@angular/core';
import { TargetingChoiceService } from './targeting-choice.service';
import { ActivatedAbilityView, Card, Game, MessageType, Permanent, StackEntry, ValidTargetsResponse, WebsocketService } from './websocket.service';

const ME = 'me';

/** Minimal untapped land Permanent — only the fields canTapPermanent reads. */
function land(id: string): Permanent {
  return {
    id,
    tapped: false,
    summoningSick: false,
    card: {
      id,
      name: id,
      type: 'LAND',
      hasTapAbility: true,
      activatedAbilities: [],
    } as unknown as Card,
  } as unknown as Permanent;
}

/** Minimal Game with `me` holding priority. */
function gameWithPriority(): Game {
  return { priorityPlayerId: ME, playerIds: [ME, 'opp'] } as unknown as Game;
}

function makeService(battlefield: Permanent[]): TargetingChoiceService {
  const ws = { currentUser: { userId: ME }, send: () => {} } as unknown as WebsocketService;
  const service = new TargetingChoiceService(ws);
  service.init(
    signal(gameWithPriority()),
    () => battlefield,
    () => [],
    () => 0,
  );
  return service;
}

describe('TargetingChoiceService land tappability during targeting', () => {
  it('marks untapped lands tappable when no target selection is in progress', () => {
    const service = makeService([land('plains')]);

    expect(service.selectingCastTarget).toBe(false);
    expect(service.canTapPermanent(0)).toBe(true);
  });

  it('suppresses land tapping while choosing a permanent/player target', () => {
    const service = makeService([land('plains')]);

    // Simulate a cast that requested valid targets and is now waiting on a pick.
    service.selectingTarget = true;

    expect(service.selectingCastTarget).toBe(true);
    expect(service.canTapPermanent(0)).toBe(false);
  });

  it('suppresses land tapping while choosing a spell target (counterspell)', () => {
    const service = makeService([land('plains')]);

    service.targetingSpell = true;

    expect(service.selectingCastTarget).toBe(true);
    expect(service.canTapPermanent(0)).toBe(false);
  });

  it('suppresses land tapping while choosing multiple targets', () => {
    const service = makeService([land('plains')]);

    service.multiTargeting = true;

    expect(service.selectingCastTarget).toBe(true);
    expect(service.canTapPermanent(0)).toBe(false);
  });

  it('suppresses land tapping while choosing a graveyard target', () => {
    const service = makeService([land('plains')]);

    service.targetingGraveyard = true;

    expect(service.selectingCastTarget).toBe(true);
    expect(service.canTapPermanent(0)).toBe(false);
  });

  it('re-enables land tapping once the target selection is cleared', () => {
    const service = makeService([land('plains')]);

    service.selectingTarget = true;
    expect(service.canTapPermanent(0)).toBe(false);

    // Cancelling targeting clears every targeting flag; lands become tappable again
    // so the player can now pay for the (still-held) cast by tapping mana.
    service.cancelTargeting();

    expect(service.selectingCastTarget).toBe(false);
    expect(service.canTapPermanent(0)).toBe(true);
  });
});

/** Activated-ability view with a mana cost — only the fields the payment flow reads. */
function abilityView(overrides: Partial<ActivatedAbilityView> = {}): ActivatedAbilityView {
  return {
    description: 'ability',
    requiresTap: false,
    needsTarget: false,
    needsSpellTarget: false,
    manaCost: '{2}',
    loyaltyCost: null,
    minTargets: 0,
    maxTargets: 0,
    isManaAbility: false,
    variableLoyaltyCost: false,
    variableCounterCostType: null,
    ...overrides,
  } as ActivatedAbilityView;
}

/** Minimal Permanent with the given activated abilities. */
function abilitySource(id: string, abilities: ActivatedAbilityView[], hasTapAbility = false): Permanent {
  return {
    id,
    tapped: false,
    summoningSick: false,
    card: {
      id,
      name: id,
      type: 'CREATURE',
      hasTapAbility,
      activatedAbilities: abilities,
    } as unknown as Card,
  } as unknown as Permanent;
}

function gameWithPool(pool: Record<string, number>, priorityPlayerId = ME): Game {
  return { priorityPlayerId, playerIds: [ME, 'opp'], manaPool: pool } as unknown as Game;
}

function makePaymentService(
  battlefield: Permanent[],
  game: WritableSignal<Game | null>,
  sent: any[],
  potentialPayableAbilities: Record<string, number[]> = {},
): TargetingChoiceService {
  const ws = { currentUser: { userId: ME }, send: (m: any) => sent.push(m) } as unknown as WebsocketService;
  const service = new TargetingChoiceService(ws);
  service.init(game, () => battlefield, () => [], () => 0, () => false, () => 0,
    () => potentialPayableAbilities);
  return service;
}

describe('TargetingChoiceService MTGO-style ability activation payment', () => {
  it('holds back an unaffordable activation and enters payment mode', () => {
    const perm = abilitySource('pumper', [abilityView()]);
    const sent: any[] = [];
    const service = makePaymentService([perm], signal(gameWithPool({})), sent, { pumper: [0] });

    service.activateAbilityAtIndex(0, 0, perm);

    expect(sent.length).toBe(0);
    expect(service.payingForAbility).toBe(true);
    expect(service.pendingActivationPermanentId).toBe('pumper');
  });

  it('sends an affordable activation immediately', () => {
    const perm = abilitySource('pumper', [abilityView()]);
    const sent: any[] = [];
    const service = makePaymentService([perm], signal(gameWithPool({ G: 2 })), sent);

    service.activateAbilityAtIndex(0, 0, perm);

    expect(sent.length).toBe(1);
    expect(sent[0].type).toBe(MessageType.ACTIVATE_ABILITY);
    expect(service.payingForAbility).toBe(false);
  });

  it('fires the held activation once the pool covers the cost', () => {
    const perm = abilitySource('pumper', [abilityView()]);
    const game = signal<Game | null>(gameWithPool({}));
    const sent: any[] = [];
    const service = makePaymentService([perm], game, sent, { pumper: [0] });
    service.activateAbilityAtIndex(0, 0, perm);

    game.set(gameWithPool({ G: 2 }));
    service.onGameStateUpdate();

    expect(sent.length).toBe(1);
    expect(sent[0].type).toBe(MessageType.ACTIVATE_ABILITY);
    expect(sent[0].permanentIndex).toBe(0);
    expect(service.payingForAbility).toBe(false);
  });

  it('cancel reverts the taps and clears payment mode', () => {
    const perm = abilitySource('pumper', [abilityView()]);
    const sent: any[] = [];
    const service = makePaymentService([perm], signal(gameWithPool({})), sent, { pumper: [0] });
    service.activateAbilityAtIndex(0, 0, perm);

    service.cancelPendingAbility();

    expect(sent.length).toBe(1);
    expect(sent[0].type).toBe(MessageType.REVERT_MANA_ACTIVATIONS);
    expect(service.payingForAbility).toBe(false);
  });

  it('abandons the held activation when priority is lost', () => {
    const perm = abilitySource('pumper', [abilityView()]);
    const game = signal<Game | null>(gameWithPool({}));
    const sent: any[] = [];
    const service = makePaymentService([perm], game, sent, { pumper: [0] });
    service.activateAbilityAtIndex(0, 0, perm);

    game.set(gameWithPool({ G: 2 }, 'opp'));
    service.onGameStateUpdate();

    expect(sent.length).toBe(0);
    expect(service.payingForAbility).toBe(false);
  });

  it('only mana production stays clickable while paying', () => {
    const pumper = abilitySource('pumper', [abilityView()]);
    const otherPumper = abilitySource('other', [abilityView({ manaCost: null })]);
    const manaLand = land('forest');
    const sent: any[] = [];
    const service = makePaymentService(
      [pumper, otherPumper, manaLand], signal(gameWithPool({})), sent, { pumper: [0] });
    service.activateAbilityAtIndex(0, 0, pumper);

    expect(service.canTapPermanent(2)).toBe(true);
    expect(service.canTapPermanent(1)).toBe(false);
  });

  it('locks the held activation\'s own {T}-cost source against mana tapping', () => {
    // A source like Doubling Cube: "{3}, {T}: ..." that could also tap for mana.
    const cube = abilitySource('cube', [abilityView({ manaCost: '{3}', requiresTap: true })], true);
    const sent: any[] = [];
    const service = makePaymentService([cube, land('forest')], signal(gameWithPool({})), sent, { cube: [0] });
    service.activateAbilityAtIndex(0, 0, cube);

    expect(service.payingForAbility).toBe(true);
    expect(service.canTapPermanent(0)).toBe(false);
    expect(service.canTapPermanent(1)).toBe(true);
  });

  it('marks an ability usable only when the server listed it as potentially payable', () => {
    const perm = abilitySource('pumper', [abilityView()]);
    const sent: any[] = [];
    const listedService = makePaymentService([perm], signal(gameWithPool({})), sent, { pumper: [0] });
    const unlistedService = makePaymentService([perm], signal(gameWithPool({})), sent);

    expect(listedService.canTapPermanent(0)).toBe(true);
    expect(unlistedService.canTapPermanent(0)).toBe(false);
  });

  it('survives the prompt window of a mana source that asks for a colour', () => {
    // Tapping a Birds of Paradise opens a colour choice, and the server reports a null priority
    // holder for as long as any interaction is awaiting input. That is not the game moving on:
    // the held activation must still be there when the colour is picked and priority returns.
    const perm = abilitySource('pumper', [abilityView()]);
    const game = signal<Game | null>(gameWithPool({}));
    const sent: any[] = [];
    const service = makePaymentService([perm], game, sent, { pumper: [0] });
    service.activateAbilityAtIndex(0, 0, perm);

    game.set(gameWithPool({}, null as unknown as string));
    service.onGameStateUpdate();

    expect(service.payingForAbility).toBe(true);
    expect(sent.length).toBe(0);

    game.set(gameWithPool({ G: 2 }));
    service.onGameStateUpdate();

    expect(sent.length).toBe(1);
    expect(sent[0].type).toBe(MessageType.ACTIVATE_ABILITY);
    expect(service.payingForAbility).toBe(false);
  });

  it('tags a mana source tapped during an ability payment with what it is paying for', () => {
    const pumper = abilitySource('pumper', [abilityView()]);
    const sent: any[] = [];
    const service = makePaymentService(
      [pumper, land('forest')], signal(gameWithPool({})), sent, { pumper: [0] });
    service.activateAbilityAtIndex(0, 0, pumper);

    service.tapPermanent(1);

    expect(sent.length).toBe(1);
    expect(sent[0].type).toBe(MessageType.TAP_PERMANENT);
    expect(sent[0].paymentIntent).toEqual({ abilityPermanentId: 'pumper', abilityIndex: 0 });
  });

  it('sends no payment intent when nothing is being paid for', () => {
    const sent: any[] = [];
    const service = makePaymentService([land('forest')], signal(gameWithPool({})), sent);

    service.tapPermanent(0);

    expect(sent.length).toBe(1);
    expect(sent[0].paymentIntent).toBeUndefined();
  });
});


describe('TargetingChoiceService planar die payment', () => {
  function planarGame(canPayRoll = false): Game {
    return { ...gameWithPool({}), turnNumber: 2, planechase: {
      faceUp: [], controllerId: ME, deckSize: 19, rollCost: 1,
      canRoll: true, canPayRoll, lastRoll: null, lastRollPlayerId: null, rollSequence: 1
    } };
  }

  it('sends an affordable roll once and waits for its result', () => {
    const game = signal<Game | null>(planarGame(true));
    const sent: any[] = [];
    const service = makePaymentService([], game, sent);
    service.rollPlanarDie();
    service.rollPlanarDie();
    expect(sent).toEqual([{ type: MessageType.ROLL_PLANAR_DIE }]);
    expect(service.planarRollPending).toBe(true);
    game.update(g => ({ ...g!, planechase: { ...g!.planechase!, rollSequence: 2 } }));
    service.onGameStateUpdate();
    expect(service.planarRollPending).toBe(false);
  });

  it('holds payment through a mana choice and rolls when mana is available', () => {
    const game = signal<Game | null>(planarGame());
    const sent: any[] = [];
    const service = makePaymentService([land('forest')], game, sent);
    service.rollPlanarDie();
    expect(service.payingForAbility).toBe(true);
    expect(service.canTapPermanent(0)).toBe(true);
    game.update(g => ({ ...g!, priorityPlayerId: null, planechase: { ...g!.planechase!, canRoll: false } }));
    service.onGameStateUpdate();
    expect(service.payingForAbility).toBe(true);
    expect(sent).toEqual([]);
    game.set(planarGame(true));
    service.onGameStateUpdate();
    expect(sent).toEqual([{ type: MessageType.ROLL_PLANAR_DIE }]);
    expect(service.payingForAbility).toBe(false);
  });

  it('abandons payment after another roll or a turn change', () => {
    for (const change of ['roll', 'turn']) {
      const game = signal<Game | null>(planarGame());
      const sent: any[] = [];
      const service = makePaymentService([], game, sent);
      service.rollPlanarDie();
      game.update(g => change === 'turn' ? { ...g!, turnNumber: 3 } :
        { ...g!, planechase: { ...g!.planechase!, rollSequence: 2, canPayRoll: true } });
      service.onGameStateUpdate();
      expect(service.payingForAbility).toBe(false);
      expect(sent).toEqual([]);
    }
  });

  it('cancels payment and requests mana reversion', () => {
    const sent: any[] = [];
    const service = makePaymentService([], signal(planarGame()), sent);
    service.rollPlanarDie();
    service.cancelPendingAbility();
    expect(service.payingForAbility).toBe(false);
    expect(sent).toEqual([{ type: MessageType.REVERT_MANA_ACTIVATIONS }]);
  });
});


describe('TargetingChoiceService source-less stack targets', () => {
  it('selects an ability only after the server has admitted its identity', () => {
    const sent: any[] = [];
    const service = makePaymentService([], signal(gameWithPool({})), sent);
    const entry = { cardId: 'planeswalk', card: null, isSpell: false,
      entryType: 'TRIGGERED_ABILITY', description: 'Planeswalk', controllerId: 'opp' } as unknown as StackEntry;
    service.targetingSpell = true;
    service.targetingSpellCardIndex = 0;
    service.selectSpellTarget(entry);
    expect(sent).toEqual([]);
    service.handleValidTargetsResponse({ type: MessageType.VALID_TARGETS_RESPONSE, validPermanentIds: ['planeswalk'], validPlayerIds: [],
      validGraveyardCardIds: [], validExiledCardIds: [], minTargets: 1, maxTargets: 1,
      prompt: 'Choose an ability' } as ValidTargetsResponse);
    expect(service.targetingSpell).toBe(true);
    service.selectSpellTarget(entry);
    expect(sent).toEqual([{ type: MessageType.PLAY_CARD, cardIndex: 0, targetId: 'planeswalk' }]);
  });
});


describe('TargetingChoiceService planar abilities', () => {
  it('routes a targeted planar ability through server target selection', () => {
    const g = gameWithPool({});
    g.planechase = { faceUp: [{ id: 'plane', card: { name: 'Test plane',
      activatedAbilities: [abilityView({ manaCost: null, needsSpellTarget: true })] } as Card,
      counters: {}, availableAbilityIndices: [0] }], controllerId: ME, deckSize: 19,
      rollCost: 0, canRoll: false, canPayRoll: false, lastRoll: null, lastRollPlayerId: null, rollSequence: 0 };
    const sent: any[] = [];
    const service = makePaymentService([], signal(g), sent);
    service.activatePlanarAbility('plane', 0);
    expect(sent[0]).toEqual({ type: MessageType.VALID_TARGETS_REQUEST, planarObjectId: 'plane', abilityIndex: 0 });
    service.handleValidTargetsResponse({ type: MessageType.VALID_TARGETS_RESPONSE,
      validPermanentIds: ['ability'], validPlayerIds: [], validGraveyardCardIds: [], validExiledCardIds: [],
      minTargets: 1, maxTargets: 1, prompt: 'Choose a target' } as ValidTargetsResponse);
    service.selectSpellTarget({ cardId: 'ability', isSpell: false } as StackEntry);
    expect(sent[1]).toEqual({ type: MessageType.ACTIVATE_PLANAR_ABILITY,
      sourceId: 'plane', abilityIndex: 0, targetId: 'ability' });
  });
});
