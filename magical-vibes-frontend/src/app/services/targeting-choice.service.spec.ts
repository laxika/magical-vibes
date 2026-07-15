import { signal } from '@angular/core';
import { TargetingChoiceService } from './targeting-choice.service';
import { Card, Game, Permanent, WebsocketService } from './websocket.service';

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
