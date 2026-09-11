package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FathomFeeder.class, GrizzlyBears.class})
class FathomFeederTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability draws a card and exiles the top card of each opponent's library")
    void drawsAndExilesOpponentsTopCard() {
        Card drawnCard = new GrizzlyBears();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLibrary(player2, List.of(exiledCard));
        harness.addToBattlefield(player1, new FathomFeeder());
        addActivationMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(handSizeBefore + 1)
                .contains(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activated ability does not exile the controller's top card")
    void exilesOnlyOpponentsLibraries() {
        Card drawnCard = new GrizzlyBears();
        Card remainingCard = new GrizzlyBears();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));
        harness.setLibrary(player2, List.of(exiledCard));
        harness.addToBattlefield(player1, new FathomFeeder());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(remainingCard, drawnCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledCard);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
