package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.n.NirkanaAssassin;
import com.github.laxika.magicalvibes.cards.c.CarrierThrall;
import com.github.laxika.magicalvibes.cards.c.CompleteDisregard;
import com.github.laxika.magicalvibes.cards.m.MindRaker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TransgressTheMind.class, NirkanaAssassin.class, CarrierThrall.class, MindRaker.class,
        CompleteDisregard.class})
class TransgressTheMindTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a chosen card with mana value 3 or greater")
    void exilesChosenCardWithManaValueAtLeastThree() {
        harness.setHand(player2, new ArrayList<>(List.of(new NirkanaAssassin(), new MindRaker())));
        castTransgressTheMind();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Nirkana Assassin"));
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Mind Raker");
    }

    @Test
    @DisplayName("Cards with mana value 2 or less are not choosable")
    void cardsBelowManaValueThreeAreExcluded() {
        harness.setHand(player2, new ArrayList<>(List.of(new CarrierThrall(), new NirkanaAssassin())));
        castTransgressTheMind();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("No choice is offered when the target has no qualifying card")
    void noChoiceWhenTargetHasNoQualifyingCard() {
        harness.setHand(player2, List.of(new CarrierThrall()));
        castTransgressTheMind();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target any player, including its caster")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new TransgressTheMind(), new NirkanaAssassin())));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Nirkana Assassin"));
    }

    @Test
    void emptyHandResolvesWithoutAChoice() {
        harness.setHand(player2, List.of());

        castTransgressTheMind();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotDeclineOrChooseAnIneligibleCard() {
        CarrierThrall ineligible = new CarrierThrall();
        NirkanaAssassin eligible = new NirkanaAssassin();
        harness.setHand(player2, List.of(ineligible, eligible));
        castTransgressTheMind();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player2, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(ineligible, eligible);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(eligible);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(ineligible);
    }

    @Test
    void canExileANoncreatureAndLeavesOtherCopiesInHand() {
        CompleteDisregard first = new CompleteDisregard();
        CompleteDisregard second = new CompleteDisregard();
        harness.setHand(player2, List.of(first, second));
        castTransgressTheMind();

        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castTransgressTheMind() {
        harness.setHand(player1, List.of(new TransgressTheMind()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
