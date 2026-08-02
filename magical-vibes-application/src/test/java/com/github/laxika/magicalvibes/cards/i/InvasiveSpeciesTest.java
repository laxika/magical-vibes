package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvasiveSpeciesTest extends BaseCardTest {

    @Test
    @DisplayName("Entering prompts the controller to return another permanent they control")
    void etbPromptsBounceOfAnotherPermanent() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new InvasiveSpecies()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        UUID islandId = harness.getPermanentId(player1, "Island");

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(islandId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("The chosen permanent is returned to its owner's hand")
    void chosenPermanentReturnsToHand() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new InvasiveSpecies()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Invasive Species");
    }

    @Test
    @DisplayName("Permanents the opponent controls are never returned")
    void opponentPermanentsNotChoices() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InvasiveSpecies()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("With no other permanent, nothing happens and it does not bounce itself")
    void noOtherPermanentNoBounce() {
        harness.setHand(player1, List.of(new InvasiveSpecies()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Invasive Species");
    }
}
