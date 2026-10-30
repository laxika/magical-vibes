package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhosphorescentFeast.class, Tarmogoyf.class, PactOfNegation.class})
class PhosphorescentFeastTest extends BaseCardTest {

    private void pay(Player player) {
        harness.addMana(player, ManaColor.GREEN, 3);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Gains 2 life for each green mana symbol among the cards left in hand")
    void gainsTwoLifePerGreenSymbol() {
        pay(player1);
        // Feast at index 0 leaves hand on cast; the two Tarmogoyfs ({1}{G} each) stay behind.
        Tarmogoyf first = new Tarmogoyf();
        Tarmogoyf second = new Tarmogoyf();
        harness.setHand(player1, List.of(new PhosphorescentFeast(), first, second));

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Counts multiple green mana symbols on one card")
    void countsMultipleGreenSymbolsOnOneCard() {
        pay(player1);
        PhosphorescentFeast other = new PhosphorescentFeast();
        harness.setHand(player1, List.of(new PhosphorescentFeast(), other));

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(other.getId()));

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Cards with no green mana symbols contribute no life")
    void noGreenSymbolsGainsZero() {
        pay(player1);
        PactOfNegation pact = new PactOfNegation();
        harness.setHand(player1, List.of(new PhosphorescentFeast(), pact));

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(pact.getId()));

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Waits for a reveal selection before granting life")
    void waitsForRevealSelection() {
        pay(player1);
        harness.setHand(player1, List.of(new PhosphorescentFeast(), new PhosphorescentFeast()));

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Revealing zero cards gains no life even with green cards in hand")
    void canRevealZeroCards() {
        pay(player1);
        harness.setHand(player1, List.of(new PhosphorescentFeast(), new PhosphorescentFeast()));

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Only revealed cards contribute green mana symbols")
    void canRevealOnlyPartOfHand() {
        pay(player1);
        PhosphorescentFeast revealed = new PhosphorescentFeast();
        harness.setHand(player1, List.of(new PhosphorescentFeast(), revealed, new Tarmogoyf()));

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(revealed.getId()));

        harness.assertLife(player1, 26);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gameLogContains("reveals")).isTrue();
    }

    @Test
    @DisplayName("An empty hand gains no life and does not count the resolving Feast")
    void emptyHandGainsNoLife() {
        pay(player1);
        harness.setHand(player1, List.of(new PhosphorescentFeast()));

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
