package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
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

    private void castAndResolve() {
        harness.setHand(player1, List.of(new RescuerChwinga()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
