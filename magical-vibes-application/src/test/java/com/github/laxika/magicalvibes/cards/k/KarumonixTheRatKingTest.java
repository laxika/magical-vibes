package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuinRat;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KarumonixTheRatKingTest extends BaseCardTest {

    @Test
    @DisplayName("Other Rats you control have toxic")
    void otherRatsYouControlHaveToxic() {
        harness.addToBattlefieldAndReturn(player1, new KarumonixTheRatKing());
        Permanent ownRat = harness.addToBattlefieldAndReturn(player1, new BogRats());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingRat = harness.addToBattlefieldAndReturn(player2, new BogRats());

        assertThat(gqs.hasKeyword(gd, ownRat, Keyword.TOXIC)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TOXIC)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingRat, Keyword.TOXIC)).isFalse();
    }

    @Test
    @DisplayName("The enter-the-battlefield ability offers any number of Rat cards from the top five")
    void etbOffersAnyNumberOfRatsFromTopFive() {
        Card firstRat = new BogRats();
        Card nonRat = new Shock();
        Card secondRat = new RuinRat();
        Card secondNonRat = new GrizzlyBears();
        Card thirdNonRat = new Shock();
        harness.setLibrary(player1, List.of(firstRat, nonRat, secondRat, secondNonRat, thirdNonRat));

        harness.setHand(player1, List.of(new KarumonixTheRatKing()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstRat.getId(), secondRat.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(firstRat.getId(), secondRat.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(firstRat, secondRat);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonRat, secondNonRat, thirdNonRat);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
