package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbrosiaWhiteheart.class, Island.class})
class AmbrosiaWhiteheartTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may return another permanent you control")
    void etbMayReturnAnotherPermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        castAmbrosia();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(island.getId());

        harness.handlePermanentChosen(player1, island.getId());

        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Ambrosia Whiteheart");
    }

    @Test
    @DisplayName("Declining the ETB ability leaves permanents on the battlefield")
    void decliningEtbAbilityDoesNothing() {
        harness.addToBattlefield(player1, new Island());

        castAmbrosia();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Ambrosia Whiteheart");
    }

    @Test
    @DisplayName("Landfall gives Ambrosia Whiteheart +1/+0 until end of turn")
    void landfallBoostsAmbrosiaWhiteheart() {
        Permanent ambrosia = harness.addToBattlefieldAndReturn(player1, new AmbrosiaWhiteheart());
        harness.setHand(player1, List.of(new Island()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(ambrosia.getEffectivePower()).isEqualTo(3);
        assertThat(ambrosia.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ambrosia.getEffectivePower()).isEqualTo(2);
        assertThat(ambrosia.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        Permanent ambrosia = harness.addToBattlefieldAndReturn(player1, new AmbrosiaWhiteheart());
        harness.setHand(player2, List.of(new Island()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(ambrosia.getEffectivePower()).isEqualTo(2);
        assertThat(ambrosia.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ambrosia can be cast during an opponent's combat")
    void flashAllowsCastingDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new Island());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        castAmbrosia();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Ambrosia Whiteheart");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("The ETB ability cannot return Ambrosia or an opponent's permanent")
    void noOtherControlledPermanentLeavesNothingToReturn() {
        harness.addToBattlefield(player2, new Island());

        castAmbrosia();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Ambrosia Whiteheart");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertNotInHand(player1, "Ambrosia Whiteheart");
    }

    @Test
    @DisplayName("A borrowed permanent returns to its owner's hand")
    void returnedPermanentGoesToOwnerInsteadOfController() {
        Island borrowedIsland = new Island();
        borrowedIsland.setOwnerId(player2.getId());
        Permanent island = harness.addToBattlefieldAndReturn(player1, borrowedIsland);

        castAmbrosia();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, island.getId());

        harness.assertInHand(player2, "Island");
        harness.assertNotInHand(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Ambrosia Whiteheart");
    }

    @Test
    @DisplayName("Landfall triggers for lands entering without being played and stacks")
    void multipleLandEntriesEachBoostAmbrosia() {
        Permanent ambrosia = harness.addToBattlefieldAndReturn(player1, new AmbrosiaWhiteheart());

        harness.enterBattlefieldAndReturn(player1, new Island());
        harness.enterBattlefieldAndReturn(player1, new Island());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ambrosia.getEffectivePower()).isEqualTo(4);
        assertThat(ambrosia.getEffectiveToughness()).isEqualTo(2);
    }

    private void castAmbrosia() {
        harness.castFromHand(player1, new AmbrosiaWhiteheart(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
