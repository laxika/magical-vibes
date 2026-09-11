package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZealousLorecasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns the chosen instant or sorcery card to hand")
    void returnsChosenInstantOrSorceryToHand() {
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock, new GrizzlyBears()));
        harness.setHand(player1, List.of(new ZealousLorecaster()));

        castZealousLorecaster();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(shock.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot decline the required graveyard target")
    void cannotDeclineRequiredTarget() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new ZealousLorecaster()));

        castZealousLorecaster();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Only instant and sorcery cards in your graveyard are valid choices")
    void excludesOtherCardsAndOpponentGraveyard() {
        Card opponentShock = new Shock();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(opponentShock));
        harness.setHand(player1, List.of(new ZealousLorecaster()));

        castZealousLorecaster();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
    }

    private void castZealousLorecaster() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
