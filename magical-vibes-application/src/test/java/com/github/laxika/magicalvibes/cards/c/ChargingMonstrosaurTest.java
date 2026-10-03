package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BishopsSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChargingMonstrosaur.class, BishopsSoldier.class})
class ChargingMonstrosaurTest extends BaseCardTest {

    @Test
    void canAttackImmediatelyAfterResolving() {
        harness.setHand(player1, List.of(new ChargingMonstrosaur()));
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Charging Monstrosaur");

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 15);
    }

    @Test
    void tramplesOverBlockerAndSurvivesCombat() {
        Permanent blocker = prepareBlockedCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Bishop's Soldier");
        harness.assertOnBattlefield(player1, "Charging Monstrosaur");
    }

    @Test
    void cannotAssignTrampleDamageBeforeLethalDamageToBlocker() {
        Permanent blocker = prepareBlockedCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample");

        harness.assertLife(player2, 20);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3));
        harness.assertLife(player2, 19);
    }

    private Permanent prepareBlockedCombat() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ChargingMonstrosaur());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new BishopsSoldier());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        return blocker;
    }
}
