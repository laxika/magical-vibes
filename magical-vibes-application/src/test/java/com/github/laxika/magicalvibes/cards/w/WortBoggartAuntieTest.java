package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CaterwaulingBoggart;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WortBoggartAuntieTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's upkeep returns Goblin from graveyard to hand")
    void upkeepReturnsGoblinFromGraveyardToHand() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        harness.setGraveyard(player1, List.of(new CaterwaulingBoggart()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Caterwauling Boggart");
        harness.assertNotInGraveyard(player1, "Caterwauling Boggart");
    }

    @Test
    @DisplayName("Returns specific Goblin when multiple Goblins are in graveyard")
    void returnsSpecificGoblinFromGraveyard() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        harness.setGraveyard(player1, List.of(new CaterwaulingBoggart(), new GoblinPiker()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInHand(player1, "Goblin Piker");
        harness.assertInGraveyard(player1, "Caterwauling Boggart");
        harness.assertNotInGraveyard(player1, "Goblin Piker");
    }

    @Test
    @DisplayName("No graveyard choice when only non-Goblin cards are present")
    void noEffectWithOnlyNonGoblinsInGraveyard() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Upkeep trigger does NOT fire during opponent's upkeep")
    void upkeepTriggerDoesNotFireDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        harness.setGraveyard(player1, List.of(new CaterwaulingBoggart()));

        advanceToUpkeep(player2);

        harness.assertInGraveyard(player1, "Caterwauling Boggart");
    }
}
