package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngrathTheFlameChained;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChargingTuskodon.class, GrizzlyBears.class, SerraAngel.class, AngrathTheFlameChained.class})
class ChargingTuskodonTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles its unblocked combat damage to a player")
    void doublesUnblockedCombatDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent tuskodon = harness.addToBattlefieldAndReturn(player1, new ChargingTuskodon());
        tuskodon.setSummoningSick(false);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Doubles only its own combat damage")
    void doesNotDoubleAnotherCreatureCombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ChargingTuskodon());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(false);

        declareAttackers(player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not double combat damage dealt to a creature")
    void doesNotDoubleCombatDamageToCreature() {
        Permanent tuskodon = harness.addToBattlefieldAndReturn(player1, new ChargingTuskodon());
        tuskodon.setSummoningSick(false);
        SerraAngel blockerCard = new SerraAngel();
        blockerCard.setToughness(5);
        harness.addToBattlefield(player2, blockerCard);

        tuskodon.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Doubles only the trample damage assigned to a player")
    void doublesTrampleDamageAssignedToPlayer() {
        harness.setLife(player2, 20);
        Permanent tuskodon = harness.addToBattlefieldAndReturn(player1, new ChargingTuskodon());
        tuskodon.setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tuskodon.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Two Tuskodons each double their own damage exactly once")
    void multipleTuskodonsDoNotMultiplyEachOthersDamage() {
        harness.setLife(player2, 20);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ChargingTuskodon());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ChargingTuskodon());
        first.setSummoningSick(false);
        second.setSummoningSick(false);

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Doubles combat damage when controlled by the other player")
    void doublesDamageForEitherController() {
        harness.setLife(player1, 20);
        Permanent tuskodon = harness.addToBattlefieldAndReturn(player2, new ChargingTuskodon());
        tuskodon.setSummoningSick(false);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Does not double combat damage to a planeswalker")
    void doesNotDoubleCombatDamageToPlaneswalker() {
        Permanent tuskodon = harness.addToBattlefieldAndReturn(player1, new ChargingTuskodon());
        Permanent angrath = harness.addToBattlefieldAndReturn(player2, new AngrathTheFlameChained());
        angrath.setCounterCount(CounterType.LOYALTY, 10);
        tuskodon.setSummoningSick(false);
        tuskodon.setAttacking(true);
        tuskodon.setAttackTarget(angrath.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(angrath.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertOnBattlefield(player2, "Angrath, the Flame-Chained");
    }
}
