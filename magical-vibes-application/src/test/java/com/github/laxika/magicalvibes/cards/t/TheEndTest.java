package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEnd.class, Forest.class, GrizzlyBears.class, NicolBolasPlaneswalker.class})
class TheEndTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles selected same-name cards and draws for cards exiled from hand")
    void exilesSelectedCardsAndDrawsForHandCopies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card graveyardCopy = new GrizzlyBears();
        Card handCopy = new GrizzlyBears();
        Card libraryCopy = new GrizzlyBears();
        Card remainingHand = new Forest();
        Card drawnCard = new Forest();

        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setHand(player2, List.of(handCopy, remainingHand));
        harness.setLibrary(player2, List.of(libraryCopy, drawnCard));
        harness.setHand(player1, List.of(new TheEnd()));
        harness.setLife(player1, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(handCopy.getId(), graveyardCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(target.getCard(), handCopy, graveyardCopy)
                .doesNotContain(libraryCopy);
        assertThat(gd.playerHands.get(player2.getId()))
                .contains(remainingHand)
                .hasSize(2)
                .containsAnyOf(libraryCopy, drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(1)
                .containsAnyOf(libraryCopy, drawnCard);
    }

    @Test
    @DisplayName("Can target a planeswalker")
    void canTargetPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new TheEnd()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Does not reduce its cost above 5 life")
    void doesNotReduceCostAboveFiveLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheEnd()));
        harness.setLife(player1, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TheEnd()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    void canChooseZeroMatchingCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card handCopy = new GrizzlyBears();
        Card graveyardCopy = new GrizzlyBears();
        Card libraryCopy = new GrizzlyBears();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy));
        harness.setHand(player1, List.of(new TheEnd()));
        harness.setLife(player1, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCopy);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCopy);
    }

    @Test
    void libraryAndGraveyardCopiesDoNotCauseDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card graveyardCopy = new GrizzlyBears();
        Card libraryCopy = new GrizzlyBears();
        Card remainingCard = new Forest();
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy, remainingCard));
        harness.setHand(player1, List.of(new TheEnd()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCopy.getId(), libraryCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(target.getCard(), graveyardCopy, libraryCopy);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
    }

    @Test
    void doesNotSearchWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card handCopy = new GrizzlyBears();
        harness.setHand(player2, List.of(handCopy));
        harness.setHand(player1, List.of(new TheEnd()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target.getCard());
    }

    @Test
    void canTargetOwnCreatureAndDrawForEachHandCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card firstCopy = new GrizzlyBears();
        Card secondCopy = new GrizzlyBears();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        Card opponentsCopy = new GrizzlyBears();
        harness.setHand(player1, List.of(new TheEnd(), firstCopy, secondCopy));
        harness.setHand(player2, List.of(opponentsCopy));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(firstCopy.getId(), secondCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(target.getCard(), firstCopy, secondCopy);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentsCopy);
    }
}
