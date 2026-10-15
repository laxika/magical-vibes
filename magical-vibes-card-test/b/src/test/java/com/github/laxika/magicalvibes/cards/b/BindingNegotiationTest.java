package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BindingNegotiation.class, Forest.class})
class BindingNegotiationTest extends BaseCardTest {

    @Test
    void choosesNonlandCardToDiscard() {
        harness.setHand(player2, List.of(new BindingNegotiation(), new Forest()));
        harness.setHand(player1, List.of(new BindingNegotiation()));
        addBindingNegotiationMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Binding Negotiation");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningChoiceOffersFaceUpExiledCard() {
        harness.setHand(player2, List.of(new BindingNegotiation()));
        harness.setExile(player2, List.of(new BindingNegotiation()));
        harness.setHand(player1, List.of(new BindingNegotiation()));
        addBindingNegotiationMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.FaceUpExiledCardChoice.class);
        UUID exiledCardId = gd.exiledCards.getFirst().card().getId();

        harness.handleMultipleCardsChosen(player1, List.of(exiledCardId));

        harness.assertInGraveyard(player2, "Binding Negotiation");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noNonlandCardStillOffersFaceUpExileChoice() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setExile(player2, List.of(new BindingNegotiation()));
        harness.setHand(player1, List.of(new BindingNegotiation()));
        addBindingNegotiationMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.FaceUpExiledCardChoice.class);
    }

    @Test
    void discardingDoesNotAlsoOfferExileChoice() {
        harness.setHand(player2, List.of(new BindingNegotiation()));
        harness.setExile(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new BindingNegotiation()));
        addBindingNegotiationMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Binding Negotiation");
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDeclineBothChoices() {
        harness.setHand(player2, List.of(new BindingNegotiation()));
        harness.setExile(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new BindingNegotiation()));
        addBindingNegotiationMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, -1);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInHand(player2, "Binding Negotiation");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyHandAllowsPuttingExiledLandIntoGraveyard() {
        harness.setHand(player2, List.of());
        Forest land = new Forest();
        harness.setExile(player2, List.of(land));
        harness.setHand(player1, List.of(new BindingNegotiation()));
        addBindingNegotiationMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void exileChoiceExcludesFaceDownCardsAndCardsOwnedByCaster() {
        harness.setHand(player2, List.of());
        Forest eligible = new Forest();
        Forest hidden = new Forest();
        Forest ownCard = new Forest();
        harness.setExile(player2, List.of(eligible));
        gd.addToExile(player2.getId(), hidden, null, true);
        harness.setExile(player1, List.of(ownCard));
        harness.setHand(player1, List.of(new BindingNegotiation()));
        addBindingNegotiationMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.FaceUpExiledCardChoice.class)
                .validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(hidden.getId(), ownCard.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noHandOrEligibleExiledCardsFinishesWithoutChoice() {
        harness.setHand(player2, List.of());
        gd.addToExile(player2.getId(), new Forest(), null, true);
        harness.setExile(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new BindingNegotiation()));
        addBindingNegotiationMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(2);
    }

    @Test
    void cannotTargetCaster() {
        harness.setHand(player1, List.of(new BindingNegotiation()));
        addBindingNegotiationMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
    private void addBindingNegotiationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
