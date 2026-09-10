package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TransgressTheMind.class, CentaurCourser.class, GrizzlyBears.class, HillGiant.class})
class TransgressTheMindTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a chosen card with mana value 3 or greater")
    void exilesChosenCardWithManaValueAtLeastThree() {
        harness.setHand(player2, new ArrayList<>(List.of(new CentaurCourser(), new HillGiant())));
        castTransgressTheMind();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Centaur Courser"));
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Hill Giant");
    }

    @Test
    @DisplayName("Cards with mana value 2 or less are not choosable")
    void cardsBelowManaValueThreeAreExcluded() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new CentaurCourser())));
        castTransgressTheMind();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("No choice is offered when the target has no qualifying card")
    void noChoiceWhenTargetHasNoQualifyingCard() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        castTransgressTheMind();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target any player, including its caster")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new TransgressTheMind(), new CentaurCourser())));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Centaur Courser"));
    }

    private void castTransgressTheMind() {
        harness.setHand(player1, List.of(new TransgressTheMind()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
