package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WardenOfTheChained.class, AirElemental.class, HillGiant.class})
class WardenOfTheChainedTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack when it is the only creature with power 4 or greater")
    void cannotAttackWithoutAnotherPowerFourCreature() {
        addCreatureReady(player1, new WardenOfTheChained());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when the other creature has power 3")
    void cannotAttackWithOnlyPowerThreeCreature() {
        addCreatureReady(player1, new WardenOfTheChained());
        addCreatureReady(player1, new HillGiant());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when controlling another creature with power 4")
    void canAttackWithAnotherPowerFourCreature() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WardenOfTheChained());
        addCreatureReady(player1, new AirElemental());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    void opponentsPowerFourCreatureDoesNotEnableAttack() {
        addCreatureReady(player1, new WardenOfTheChained());
        addCreatureReady(player2, new WardenOfTheChained());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedSummoningSickCreatureStillEnablesAttack() {
        addCreatureReady(player1, new WardenOfTheChained());
        Permanent support = addCreatureReady(player1, new WardenOfTheChained());
        support.tap();
        support.setSummoningSick(true);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void twoWardensCanAttackTogether() {
        addCreatureReady(player1, new WardenOfTheChained());
        addCreatureReady(player1, new WardenOfTheChained());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    void reducedPowerOfOtherCreaturePreventsAttack() {
        addCreatureReady(player1, new WardenOfTheChained());
        Permanent support = addCreatureReady(player1, new WardenOfTheChained());
        support.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBlockAloneAndAttackingWardenTramplesOverBlocker() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WardenOfTheChained());
        addCreatureReady(player1, new WardenOfTheChained());
        Permanent blocker = addCreatureReady(player2, new WardenOfTheChained());
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Warden of the Chained");
        assertThat(countPermanents(player1, "Warden of the Chained")).isEqualTo(2);
    }
}
