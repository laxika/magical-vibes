package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Genesis.class, GrizzlyBears.class})
class GenesisTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{G} returns the chosen creature card from the graveyard to hand")
    void payingUpkeepCostReturnsTargetCreatureToHand() {
        Genesis genesis = new Genesis();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(genesis, bears));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Genesis");
    }

    @Test
    @DisplayName("Declining the upkeep payment leaves the targeted creature card in the graveyard")
    void decliningUpkeepPaymentLeavesTargetInGraveyard() {
        Genesis genesis = new Genesis();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(genesis, bears));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Genesis");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The graveyard upkeep ability triggers only during Genesis's owner's upkeep")
    void triggersOnlyDuringOwnersUpkeep() {
        harness.setGraveyard(player1, List.of(new Genesis(), new GrizzlyBears()));

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
