package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GandalfOfTheSecretFire.class, Shock.class})
class GandalfOfTheSecretFireTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a hand-cast instant with three time counters after it resolves")
    void exilesHandCastInstantWithSuspendCountersAfterResolution() {
        harness.addToBattlefield(player1, new GandalfOfTheSecretFire());
        enterOpponentTurn();

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(shock.getId(), player1.getId(), 3));
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Does not trigger for a hand-cast instant during its controller's turn")
    void doesNotTriggerDuringControllerTurn() {
        harness.addToBattlefield(player1, new GandalfOfTheSecretFire());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.findExiledCard(shock.getId())).isNull();
        assertThat(gd.suspendedSpellExiles).isEmpty();
    }

    private void enterOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
