package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FloriferousVinewall.class, Plains.class, GrizzlyBears.class, Shock.class})
class FloriferousVinewallTest extends BaseCardTest {

    @Test
    void mayRevealALandFromTheTopSixIntoHand() {
        Card land = new Plains();
        List<Card> topCards = List.of(
                new Shock(), new GrizzlyBears(), land,
                new Shock(), new GrizzlyBears(), new Shock());
        harness.setLibrary(player1, topCards);
        harness.setHand(player1, List.of(new FloriferousVinewall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(land.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(List.of(
                        topCards.get(0), topCards.get(1), topCards.get(3),
                        topCards.get(4), topCards.get(5)));
    }

    @Test
    void decliningPutsAllTopSixCardsOnTheBottom() {
        List<Card> topCards = List.of(
                new Plains(), new Shock(), new GrizzlyBears(),
                new Shock(), new GrizzlyBears(), new Shock());
        harness.setLibrary(player1, topCards);
        harness.setHand(player1, List.of(new FloriferousVinewall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void withNoLandAmongTopSixAllCardsGoToTheBottomWithoutAChoice() {
        List<Card> topCards = List.of(
                new Shock(), new GrizzlyBears(), new Shock(),
                new GrizzlyBears(), new Shock(), new GrizzlyBears());
        harness.setLibrary(player1, topCards);
        harness.setHand(player1, List.of(new FloriferousVinewall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards);
    }
}
