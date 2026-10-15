package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tervigon.class, GrizzlyBears.class})
class TervigonTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous does not draw below X=5")
    void ravenousDoesNotDrawBelowThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent tervigon = castTervigon(4);

        assertThat(tervigon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ravenous enters with X +1/+1 counters and draws at X=5")
    void ravenousEntersWithCountersAndDrawsAtThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent tervigon = castTervigon(5);

        assertThat(tervigon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Combat damage to a player creates that many Tyranid tokens")
    void createsTokensEqualToCombatDamage() {
        Permanent tervigon = castTervigon(3);
        tervigon.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(findPermanents(player1, "Tyranid")).hasSize(3);
    }

    @Test
    void zeroXDiesWithoutDrawing() {
        harness.setLibrary(player1, List.of(new Tervigon()));
        harness.setHand(player1, List.of(new Tervigon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tervigon");
        harness.assertInGraveyard(player1, "Tervigon");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void trampleCreatesTokensOnlyForDamageDealtToPlayer() {
        Permanent tervigon = castTervigon(4);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tervigon.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 2));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(findPermanents(player1, "Tyranid")).hasSize(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void damageOnlyToBlockerCreatesNoTokens() {
        Permanent tervigon = castTervigon(2);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tervigon.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Tyranid")).isEmpty();
        harness.assertInGraveyard(player1, "Tervigon");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void tokenTriggerRetainsDamageAfterSourceLeavesBattlefield() {
        Permanent tervigon = castTervigon(3);
        tervigon.setAttacking(true);
        harness.resolveCombatDamage();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, tervigon));

        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(findPermanents(player1, "Tyranid")).hasSize(3);
    }

    @Test
    void ravenousDrawStillResolvesAfterSourceLeavesBattlefield() {
        harness.setLibrary(player1, List.of(new Tervigon()));
        harness.setHand(player1, List.of(new Tervigon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0, 5);
        harness.passBothPriorities();
        Permanent tervigon = findPermanent(player1, "Tervigon");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, tervigon));

        resolveAllTriggers();

        harness.assertInHand(player1, "Tervigon");
    }

    private Permanent castTervigon(int x) {
        harness.setHand(player1, List.of(new Tervigon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x + 1);

        harness.castCreature(player1, 0, x);
        resolveAllTriggers();
        return findPermanent(player1, "Tervigon");
    }
}
