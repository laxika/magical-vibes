package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MerfolkTradersTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card, then discards a card")
    void entersAndLoots() {
        setDeck(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MerfolkTraders(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve the ETB trigger

        // No "may" prompt: the loot is mandatory, so we go straight to the discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        // Cast Traders (-1), drew 1, discarded 1 -> net -1 vs before
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isNotEmpty();
    }

    @Test
    @DisplayName("The freshly drawn card can itself be discarded")
    void drawnCardIsDiscardable() {
        setDeck(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MerfolkTraders()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Hand was empty after casting; the only discardable card is the one just drawn
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
