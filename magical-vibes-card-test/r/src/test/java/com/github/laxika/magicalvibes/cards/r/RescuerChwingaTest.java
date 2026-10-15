package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RescuerChwinga.class, GrizzlyBears.class, Island.class})
class RescuerChwingaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers to return another permanent you control")
    void etbOffersOptionalReturn() {
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");

        castAndResolve();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        UUID chwingaId = harness.getPermanentId(player1, "Rescuer Chwinga");
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(islandId)
                .doesNotContain(chwingaId);
    }

    @Test
    @DisplayName("Accepting the may ability returns the chosen permanent")
    void acceptingReturnsChosenPermanent() {
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");

        castAndResolve();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, islandId);

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Rescuer Chwinga");
    }

    @Test
    @DisplayName("Declining the may ability leaves permanents on the battlefield")
    void decliningLeavesPermanentsOnBattlefield() {
        harness.addToBattlefield(player1, new Island());

        castAndResolve();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Rescuer Chwinga");
    }

    @Test
    @DisplayName("Only permanents you control other than the source are choices")
    void onlyOtherControlledPermanentsAreChoices() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID islandId = harness.getPermanentId(player1, "Island");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castAndResolve();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(islandId)
                .doesNotContain(bearsId);
    }

    @Test
    @DisplayName("Accepting with no other permanent cannot return the source")
    void noOtherPermanentLeavesSourceOnBattlefield() {
        castAndResolve();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rescuer Chwinga");
        harness.assertNotInHand(player1, "Rescuer Chwinga");
    }

    @Test
    @DisplayName("Another Rescuer Chwinga can be returned without returning the source")
    void returnsAnotherCreatureOfTheSameName() {
        RescuerChwinga earlierChwinga = new RescuerChwinga();
        UUID earlierId = harness.addToBattlefieldAndReturn(player1, earlierChwinga).getId();

        castAndResolve();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(earlierId);
        harness.handlePermanentChosen(player1, earlierId);

        assertThat(gd.playerHands.get(player1.getId())).contains(earlierChwinga);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .noneMatch(permanent -> permanent.getId().equals(earlierId));
        harness.assertOnBattlefield(player1, "Rescuer Chwinga");
    }

    @Test
    @DisplayName("A controlled permanent returns to its owner's hand")
    void returnsBorrowedPermanentToOwner() {
        Island borrowedIsland = new Island();
        borrowedIsland.setOwnerId(player2.getId());
        UUID islandId = harness.addToBattlefieldAndReturn(player1, borrowedIsland).getId();

        castAndResolve();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, islandId);

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.playerHands.get(player2.getId())).contains(borrowedIsland);
        harness.assertOnBattlefield(player1, "Rescuer Chwinga");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");
        harness.passUntil(player2, TurnStep.UPKEEP);

        castAndResolve();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, islandId);

        harness.assertOnBattlefield(player1, "Rescuer Chwinga");
        harness.assertInHand(player1, "Island");
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new RescuerChwinga(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
