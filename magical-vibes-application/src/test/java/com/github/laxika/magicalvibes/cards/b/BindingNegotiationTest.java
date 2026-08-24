package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BindingNegotiation.class, Peek.class, Forest.class})
class BindingNegotiationTest extends BaseCardTest {

    @Test
    void choosesNonlandCardToDiscard() {
        harness.setHand(player2, List.of(new Peek(), new Forest()));
        harness.setHand(player1, List.of(new BindingNegotiation()));
        addBindingNegotiationMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Peek");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningChoiceOffersFaceUpExiledCard() {
        harness.setHand(player2, List.of(new Peek()));
        harness.setExile(player2, List.of(new Peek()));
        harness.setHand(player1, List.of(new BindingNegotiation()));
        addBindingNegotiationMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.FaceUpExiledCardChoice.class);
        UUID exiledCardId = gd.exiledCards.getFirst().card().getId();

        harness.handleMultipleCardsChosen(player1, List.of(exiledCardId));

        harness.assertInGraveyard(player2, "Peek");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noNonlandCardStillOffersFaceUpExileChoice() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setExile(player2, List.of(new Peek()));
        harness.setHand(player1, List.of(new BindingNegotiation()));
        addBindingNegotiationMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.FaceUpExiledCardChoice.class);
    }

    private void addBindingNegotiationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
