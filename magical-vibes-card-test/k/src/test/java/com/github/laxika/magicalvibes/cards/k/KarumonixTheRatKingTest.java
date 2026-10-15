package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.b.BlightbellyRat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuinRat;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarumonixTheRatKing.class, BogRats.class, GrizzlyBears.class, RuinRat.class, Shock.class,
        BlightbellyRat.class})
class KarumonixTheRatKingTest extends BaseCardTest {

    @Test
    @DisplayName("Other Rats you control have toxic")
    void otherRatsYouControlHaveToxic() {
        harness.addToBattlefield(player1, new KarumonixTheRatKing());
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

        harness.castFromHand(player1, new KarumonixTheRatKing(), "{1}{B}{B}");
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

    @Test
    void karumonixCombatDamageGivesOnePoisonCounter() {
        Permanent king = harness.addToBattlefieldAndReturn(player1, new KarumonixTheRatKing());
        king.setSummoningSick(false);
        king.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void grantedToxicAddsToExistingToxic() {
        harness.addToBattlefield(player1, new KarumonixTheRatKing());
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new BlightbellyRat());
        rat.setSummoningSick(false);
        rat.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void mayDeclineAllRatsFromShortLibrary() {
        Card rat = new KarumonixTheRatKing();
        harness.setLibrary(player1, List.of(rat));
        harness.castFromHand(player1, new KarumonixTheRatKing(), "{1}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(rat);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayChooseOnlySomeRatsAndLeavesSixthCardOnTop() {
        Card selected = new KarumonixTheRatKing();
        Card declined = new BlightbellyRat();
        Card third = new KarumonixTheRatKing();
        Card fourth = new BlightbellyRat();
        Card fifth = new KarumonixTheRatKing();
        Card sixth = new BlightbellyRat();
        harness.setLibrary(player1, List.of(selected, declined, third, fourth, fifth, sixth));
        harness.castFromHand(player1, new KarumonixTheRatKing(), "{1}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).doesNotContain(sixth.getId());
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                sixth, declined, third, fourth, fifth);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(sixth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
