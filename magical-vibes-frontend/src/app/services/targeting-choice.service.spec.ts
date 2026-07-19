import { signal, WritableSignal } from '@angular/core';
import { TargetingChoiceService } from './targeting-choice.service';
import { ActivatedAbilityView, Card, Game, MessageType, Permanent, WebsocketService } from './websocket.service';

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
});
