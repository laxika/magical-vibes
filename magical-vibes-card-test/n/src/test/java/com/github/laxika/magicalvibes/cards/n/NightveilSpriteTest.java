package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DimirInformant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightveilSprite.class, DimirInformant.class})
class NightveilSpriteTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking surveils 1 and may put the top card into the graveyard")
    void attackingSurveilsOne() {
        Card topCard = new DimirInformant();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        addCreatureReady(player1, new NightveilSprite());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Choosing to keep the surveilled card leaves it on top of the library")
    void decliningSurveilLeavesTopCardOnLibrary() {
        Card topCard = new DimirInformant();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        addCreatureReady(player1, new NightveilSprite());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Surveil mills exactly one card and leaves the opponent's library alone")
    void surveilsOnlyControllersTopCard() {
        Card topCard = new DimirInformant();
        Card secondCard = new DimirInformant();
        Card opponentCard = new DimirInformant();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setLibrary(player2, List.of(opponentCard));
        addCreatureReady(player1, new NightveilSprite());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Surveilling an empty library still counts as surveilling")
    void surveilsEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        addCreatureReady(player1, new NightveilSprite());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playersWhoSurveilledThisTurn).contains(player1.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Keeping the card still counts as surveilling")
    void keepingCardStillSurveils() {
        Card topCard = new DimirInformant();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new NightveilSprite());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playersWhoSurveilledThisTurn).contains(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
