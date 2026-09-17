package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LastRites.class, AvenFisher.class, Forest.class, Peek.class})
class LastRitesTest extends BaseCardTest {

    @Test
    @DisplayName("Discards one nonland card from the target's hand for each card discarded")
    void discardsOneNonlandCardPerCardDiscarded() {
        harness.setHand(player1, List.of(new LastRites(), new AvenFisher(), new AvenFisher()));
        harness.setHand(player2, List.of(new Peek(), new Forest(), new AvenFisher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gameLogContains("reveals their hand")).isTrue();
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 2);
        assertThat(choice.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Peek", "Aven Fisher");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Last Rites", "Aven Fisher", "Aven Fisher");
    }

    @Test
    @DisplayName("Discarding zero cards leaves the target hand unchanged")
    void discardingZeroCardsDoesNothingToTargetHand() {
        harness.setHand(player1, List.of(new LastRites(), new AvenFisher()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Peek");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Aven Fisher");
        harness.assertInGraveyard(player1, "Last Rites");
    }

    @Test
    @DisplayName("Lands cannot be chosen for the target's discards")
    void landsAreExcludedFromTargetChoices() {
        harness.setHand(player1, List.of(new LastRites(), new AvenFisher()));
        harness.setHand(player2, List.of(new Forest(), new Peek()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Chooses every available nonland when fewer are available than cards discarded")
    void choosesEveryAvailableNonlandWhenTargetHasFewerValidCards() {
        harness.setHand(player1, List.of(
                new LastRites(), new AvenFisher(), new AvenFisher(), new AvenFisher()));
        harness.setHand(player2, List.of(new Forest(), new Peek()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleXValueChosen(player1, 3);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        assertThat(choice.remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Peek");
    }

    @Test
    @DisplayName("Does nothing when the target has no nonland cards")
    void doesNothingWhenTargetHasNoNonlandCards() {
        harness.setHand(player1, List.of(new LastRites(), new AvenFisher()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target the caster's own hand")
    void canTargetTheCaster() {
        harness.setHand(player1, List.of(new LastRites(), new AvenFisher(), new AvenFisher()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Last Rites", "Aven Fisher", "Aven Fisher");
    }
}
