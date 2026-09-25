package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InquisitorCaptain.class, BeskirShieldmate.class, GrizzlyBears.class})
class InquisitorCaptainTest extends BaseCardTest {

    @Test
    void seeksTwoEligibleCreaturesAndPutsTheChosenOneOntoBattlefield() {
        InquisitorCaptain captain = new InquisitorCaptain();
        GrizzlyBears soughtBears = new GrizzlyBears();
        BeskirShieldmate soughtShieldmate = new BeskirShieldmate();
        setUpThreshold(captain, 9, 9, List.of(soughtBears, soughtShieldmate));

        castCaptain(captain);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                soughtBears.getId(), soughtShieldmate.getId());

        harness.handleMultipleCardsChosen(player1, List.of(soughtBears.getId()));

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player1, "Beskir Shieldmate")).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).contains(soughtShieldmate);
    }

    @Test
    void doesNotSeekWithFewerThanTwentyEligibleCards() {
        InquisitorCaptain captain = new InquisitorCaptain();
        GrizzlyBears soughtBears = new GrizzlyBears();
        BeskirShieldmate soughtShieldmate = new BeskirShieldmate();
        setUpThreshold(captain, 8, 9, List.of(soughtBears, soughtShieldmate));

        castCaptain(captain);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(soughtBears, soughtShieldmate);
    }

    @Test
    void doesNotSeekWhenItEntersWithoutBeingCast() {
        InquisitorCaptain captain = new InquisitorCaptain();
        setUpThreshold(captain, 9, 9, List.of(new GrizzlyBears(), new BeskirShieldmate()));

        harness.enterBattlefieldAndReturn(player1, captain);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
    }

    private void setUpThreshold(InquisitorCaptain captain, int handCount, int graveyardCount,
                                List<Card> libraryCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(captain);
        hand.addAll(cards(handCount));
        harness.setHand(player1, hand);
        harness.setGraveyard(player1, cards(graveyardCount));
        harness.setLibrary(player1, libraryCards);
    }

    private void castCaptain(InquisitorCaptain captain) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
