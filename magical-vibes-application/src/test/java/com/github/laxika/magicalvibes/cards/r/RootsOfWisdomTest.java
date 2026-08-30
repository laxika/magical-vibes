package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RootsOfWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("Mills three cards and returns a land from the graveyard")
    void returnsLandFromGraveyard() {
        Forest forest = new Forest();
        setGraveyard(forest);
        setLibrary(new Shock(), new GrizzlyBears(), new Shock());

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Shock", "Grizzly Bears", "Shock", "Roots of Wisdom");
    }

    @Test
    @DisplayName("Returns an Elf from the graveyard")
    void returnsElfFromGraveyard() {
        LlanowarElves elves = new LlanowarElves();
        setGraveyard(elves);
        setLibrary(new Shock(), new GrizzlyBears(), new Shock());

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Shock", "Grizzly Bears", "Shock", "Roots of Wisdom");
    }

    @Test
    @DisplayName("Draws a card when no land or Elf is in the graveyard")
    void drawsWhenNoReturnableCardExists() {
        setGraveyard(new GrizzlyBears());
        Forest forest = new Forest();
        setLibrary(new Shock(), new GrizzlyBears(), new Shock(), forest);

        castAndResolve();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Shock", "Grizzly Bears", "Shock", "Roots of Wisdom");
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new RootsOfWisdom()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }

    private void setGraveyard(Card... cards) {
        harness.setGraveyard(player1, List.of(cards));
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
