package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SwordwiseCentaur;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChargingBadger.class, SwordwiseCentaur.class})
class ChargingBadgerTest extends BaseCardTest {

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);

        Permanent badger = harness.addToBattlefieldAndReturn(player1, new ChargingBadger());
        badger.setPowerModifier(2);
        badger.setSummoningSick(false);
        badger.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Trample cannot assign damage to the player before lethal damage to the blocker")
    void cannotTrampleWithoutAssigningLethalDamage() {
        harness.setLife(player2, 20);
        Permanent badger = harness.addToBattlefieldAndReturn(player1, new ChargingBadger());
        badger.setPowerModifier(2);
        badger.setSummoningSick(false);
        badger.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample");
        harness.assertLife(player2, 20);

        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 1));
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Trample permits assigning all damage to the blocker")
    void mayAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent badger = harness.addToBattlefieldAndReturn(player1, new ChargingBadger());
        badger.setPowerModifier(2);
        badger.setSummoningSick(false);
        badger.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Swordwise Centaur");
        harness.assertInGraveyard(player1, "Charging Badger");
    }
}
