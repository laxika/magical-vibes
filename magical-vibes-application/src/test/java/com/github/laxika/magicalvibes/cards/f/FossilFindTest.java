package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FossilFind.class, GrizzlyBears.class, LlanowarElves.class})
class FossilFindTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one card at random from graveyard to hand")
    void returnsOneCardFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new FossilFind()));
        harness.setHand(player1, List.of(new FossilFind()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Exactly one of the three graveyard cards returns to hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Two remaining cards + Fossil Find itself go to graveyard after resolution.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Returns the only card when graveyard has one card")
    void returnsTheOnlyCard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new FossilFind()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        // Only Fossil Find itself in graveyard after resolution.
        harness.assertInGraveyard(player1, "Fossil Find");
    }

    @Test
    @DisplayName("Does nothing when graveyard is empty")
    void doesNothingWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new FossilFind()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can return a noncreature card and can be paid for with red mana")
    void returnsSorceryWithRedMana() {
        FossilFind graveyardCard = new FossilFind();
        FossilFind spell = new FossilFind();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Does not return cards from an opponent's graveyard")
    void ignoresOpponentsGraveyard() {
        FossilFind opponentsCard = new FossilFind();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentsCard));
        harness.setHand(player1, List.of(new FossilFind()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCard);
    }

    @Test
    @DisplayName("Offers graveyard reordering after returning the random card")
    void offersGraveyardReordering() {
        List<Card> candidates = List.of(new FossilFind(), new FossilFind(), new FossilFind());
        FossilFind spell = new FossilFind();
        harness.setGraveyard(player1, candidates);
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(candidates).contains(gd.playerHands.get(player1.getId()).getFirst());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).doesNotContain(spell);
    }
}
