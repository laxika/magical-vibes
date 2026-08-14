package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AmbushWolfTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts for up to one graveyard target before ability goes on stack")
    void etbPromptsForGraveyardTarget() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        castAmbushWolf();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ambush Wolf");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount())
                .isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB exiles the chosen card from a graveyard")
    void etbExilesChosenCard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        castAmbushWolf();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB can exile a card from its controller's graveyard")
    void etbExilesOwnCard() {
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castAmbushWolf();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
    }

    @Test
    @DisplayName("ETB can choose zero cards")
    void etbCanChooseZeroCards() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        castAmbushWolf();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB with empty graveyards resolves without a target prompt")
    void etbWithEmptyGraveyards() {
        castAmbushWolf();

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ambush Wolf");
    }

    private void castAmbushWolf() {
        harness.setHand(player1, List.of(new AmbushWolf()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
    }
}
