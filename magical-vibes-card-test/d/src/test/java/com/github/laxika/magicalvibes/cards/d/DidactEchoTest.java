package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AncestralReminiscence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PanickedAltisaur;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DidactEcho.class, Forest.class, PanickedAltisaur.class, Island.class, AncestralReminiscence.class})
class DidactEchoTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws a card")
    void entersAndDrawsCard() {
        Island island = new Island();
        harness.setLibrary(player1, List.of(island));
        harness.castFromHand(player1, new DidactEcho(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
    }

    @Test
    @DisplayName("Has flying with four permanent cards in its controller's graveyard")
    void hasFlyingAtFourPermanentCards() {
        Permanent echo = harness.addToBattlefieldAndReturn(player1, new DidactEcho());
        harness.setGraveyard(player1, List.of(
                new Forest(), new PanickedAltisaur(), new Island(), new Forest()));

        assertThat(gqs.hasKeyword(gd, echo, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Nonpermanent cards do not count toward flying")
    void nonpermanentCardsDoNotCount() {
        Permanent echo = harness.addToBattlefieldAndReturn(player1, new DidactEcho());
        List<Card> graveyard = List.of(new AncestralReminiscence(), new AncestralReminiscence(),
                new AncestralReminiscence(), new AncestralReminiscence());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.hasKeyword(gd, echo, Keyword.FLYING)).isFalse();

        harness.setGraveyard(player1, List.of(
                new Forest(), new PanickedAltisaur(), new Island(), new AncestralReminiscence()));

        assertThat(gqs.hasKeyword(gd, echo, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying updates immediately when the permanent-card count crosses four")
    void flyingUpdatesWithGraveyard() {
        Permanent echo = harness.addToBattlefieldAndReturn(player1, new DidactEcho());
        harness.setGraveyard(player1, List.of(new Forest(), new Island(), new PanickedAltisaur()));
        assertThat(gqs.hasKeyword(gd, echo, Keyword.FLYING)).isFalse();

        harness.setGraveyard(player1, List.of(
                new Forest(), new Island(), new PanickedAltisaur(), new Forest(), new Island()));
        assertThat(gqs.hasKeyword(gd, echo, Keyword.FLYING)).isTrue();

        harness.setGraveyard(player1, List.of(new Forest(), new Island(), new PanickedAltisaur()));
        assertThat(gqs.hasKeyword(gd, echo, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Each Echo checks only its controller's graveyard and grants flying only to itself")
    void flyingIsLimitedToSelfAndControllerGraveyard() {
        Permanent ownEcho = harness.addToBattlefieldAndReturn(player1, new DidactEcho());
        Permanent opposingEcho = harness.addToBattlefieldAndReturn(player2, new DidactEcho());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new PanickedAltisaur());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Island(), new Island()));

        assertThat(gqs.hasKeyword(gd, ownEcho, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingEcho, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The draw trigger still draws exactly one card when descend four is satisfied")
    void drawsOneCardWithDescendFour() {
        Island drawnCard = new Island();
        Forest remainingCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Island(), new Island()));
        harness.castFromHand(player1, new DidactEcho(), "{4}{U}");

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }
}
