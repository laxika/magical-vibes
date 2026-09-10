package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AzamiLadyOfScrolls;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajuruParagon.class, AzamiLadyOfScrolls.class, GrizzlyBears.class})
class TajuruParagonTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotLookAtLibrary() {
        harness.setHand(player1, List.of(new TajuruParagon()));
        harness.setLibrary(player1, List.of(new AzamiLadyOfScrolls()));
        addMana(1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void kickedMayPutOneMatchingCreatureTypeCardIntoHand() {
        Card matchingCard = new AzamiLadyOfScrolls();
        Card nonmatchingCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new TajuruParagon()));
        harness.setLibrary(player1, List.of(matchingCard, nonmatchingCard));
        addMana(4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(matchingCard.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(matchingCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(matchingCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatchingCard);
    }

    @Test
    void kickedWithNoMatchingCardBottomsRevealedCards() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setHand(player1, List.of(new TajuruParagon()));
        harness.setLibrary(player1, List.of(first, second));
        addMana(4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    private void addMana(int colorless) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }
}
