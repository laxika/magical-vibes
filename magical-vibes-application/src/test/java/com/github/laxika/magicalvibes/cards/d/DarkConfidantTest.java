package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkConfidant.class, DimirSignet.class, Forest.class})
class DarkConfidantTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of its controller's upkeep, reveals the top card, puts it into hand, and loses life equal to its mana value")
    void revealsTopCardAndLosesLifeEqualToManaValue() {
        harness.addToBattlefield(player1, new DarkConfidant());
        harness.setHand(player1, List.of());
        Card topCard = new DimirSignet();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Revealing a land puts it into your hand without losing life")
    void revealingLandCausesNoLifeLoss() {
        harness.addToBattlefield(player1, new DarkConfidant());
        harness.setHand(player1, List.of());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new DarkConfidant());
        harness.setHand(player1, List.of());
        Card topCard = new DimirSignet();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Does nothing when its controller's library is empty")
    void doesNothingWhenLibraryIsEmpty() {
        harness.addToBattlefield(player1, new DarkConfidant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Dark Confidant reveals the next card as its own trigger resolves")
    void multipleCopiesRevealSuccessiveCards() {
        harness.addToBattlefield(player1, new DarkConfidant());
        harness.addToBattlefield(player1, new DarkConfidant());
        harness.setHand(player1, List.of());
        Card firstCard = new DimirSignet();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The upkeep trigger still resolves after Dark Confidant leaves the battlefield")
    void triggerResolvesWithoutSourceOnBattlefield() {
        harness.addToBattlefield(player1, new DarkConfidant());
        harness.setHand(player1, List.of());
        Card topCard = new DimirSignet();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
