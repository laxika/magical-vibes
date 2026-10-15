package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CandlesOfLeng.class, ThinkTwice.class, AncestralVision.class})
class CandlesOfLengTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the revealed card into the graveyard when its name is already there")
    void matchingNamePutsRevealedCardIntoGraveyard() {
        Card graveyardCard = new ThinkTwice();
        Card revealedCard = new ThinkTwice();
        Card nextCard = new AncestralVision();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(revealedCard, nextCard));
        addReadyCandles();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activateAndResolve();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(graveyardCard.getId(), revealedCard.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(nextCard.getId());
    }

    @Test
    @DisplayName("Draws the revealed card when its name is not in the graveyard")
    void nonmatchingNameDrawsRevealedCard() {
        Card graveyardCard = new AncestralVision();
        Card revealedCard = new ThinkTwice();
        Card nextCard = new CandlesOfLeng();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(revealedCard, nextCard));
        addReadyCandles();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activateAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(revealedCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(graveyardCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(nextCard.getId());
    }

    @Test
    @DisplayName("Attempts to draw and loses when the library is empty")
    void emptyLibraryCausesDrawLoss() {
        Card graveyardCard = new ThinkTwice();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of());
        addReadyCandles();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activateAndResolve();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(graveyardCard.getId());
    }

    private void addReadyCandles() {
        var candles = harness.addToBattlefieldAndReturn(player1, new CandlesOfLeng());
        candles.setSummoningSick(false);
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A matching name in the opponent's graveyard does not prevent drawing")
    void ignoresOpponentsGraveyard() {
        Card revealedCard = new ThinkTwice();
        Card opponentCard = new ThinkTwice();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setLibrary(player1, List.of(revealedCard));
        addReadyCandles();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activateAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    @DisplayName("Checks graveyard names when the ability resolves")
    void checksGraveyardAtResolution() {
        Card revealedCard = new ThinkTwice();
        Card graveyardCard = new ThinkTwice();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(revealedCard));
        addReadyCandles();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard, revealedCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("An artifact can activate immediately and pays four mana and taps")
    void newlyEnteredArtifactPaysActivationCosts() {
        Card revealedCard = new ThinkTwice();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(revealedCard));
        var candles = harness.addToBattlefieldAndReturn(player1, new CandlesOfLeng());
        candles.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(candles.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealedCard);
    }

    @Test
    @DisplayName("Three mana cannot pay the four-mana activation cost")
    void cannotActivateWithOnlyThreeMana() {
        addReadyCandles();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped artifact cannot activate its tap ability")
    void cannotActivateWhenTapped() {
        var candles = harness.addToBattlefieldAndReturn(player1, new CandlesOfLeng());
        candles.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
