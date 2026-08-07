package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FallowWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a land card keeps Fallow Wurm on the battlefield")
    void discardingLandKeepsWurm() {
        resolveWurmWith(List.of(new Forest()));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Fallow Wurm");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the discard sacrifices Fallow Wurm")
    void decliningSacrificesWurm() {
        resolveWurmWith(List.of(new Forest()));

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Fallow Wurm");
        harness.assertInGraveyard(player1, "Fallow Wurm");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Auto-sacrifices when the controller has no land card in hand")
    void autoSacrificesWithoutLand() {
        harness.setHand(player1, List.of(new FallowWurm()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fallow Wurm");
        harness.assertInGraveyard(player1, "Fallow Wurm");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only land cards are offered for the discard")
    void onlyLandsAreValidDiscards() {
        resolveWurmWith(List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest()));

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactlyInAnyOrder(1, 3);
    }

    /**
     * Casts Fallow Wurm, swaps the controller's hand for {@code hand}, and resolves the
     * creature spell and its enters-the-battlefield trigger up to the may-ability prompt.
     */
    private void resolveWurmWith(List<com.github.laxika.magicalvibes.model.Card> hand) {
        harness.setHand(player1, List.of(new FallowWurm()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.setHand(player1, hand);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
