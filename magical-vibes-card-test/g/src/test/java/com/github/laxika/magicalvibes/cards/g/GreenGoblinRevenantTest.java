package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WildGuess;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreenGoblinRevenant.class, Forest.class, Mountain.class, WildGuess.class})
class GreenGoblinRevenantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking discards a card, then draws for every card discarded this turn")
    void attackingCountsAllDiscardsThisTurn() {
        harness.setHand(player1, List.of(new WildGuess(), new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        addCreatureReady(player1, new GreenGoblinRevenant());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attack discard is included in the number of cards drawn")
    void attackingDiscardsBeforeDrawing() {
        Forest discarded = new Forest();
        Mountain drawn = new Mountain();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn, new Mountain()));
        addCreatureReady(player1, new GreenGoblinRevenant());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An empty hand with no earlier discards draws no cards")
    void attackingWithEmptyHandAndNoDiscardsDrawsNothing() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        addCreatureReady(player1, new GreenGoblinRevenant());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty hand still draws for cards discarded earlier that turn")
    void attackingWithEmptyHandStillCountsEarlierDiscards() {
        harness.setHand(player1, List.of(new WildGuess(), new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();
        harness.setHand(player1, List.of());
        addCreatureReady(player1, new GreenGoblinRevenant());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cards discarded by an opponent do not increase the attack draw")
    void attackingIgnoresOpponentDiscards() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new WildGuess(), new Forest()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castSorceryWithDiscard(player2, 0, 1);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain()));
        addCreatureReady(player1, new GreenGoblinRevenant());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }
}
