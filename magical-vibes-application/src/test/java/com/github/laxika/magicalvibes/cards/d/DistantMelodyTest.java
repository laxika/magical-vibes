package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DistantMelodyTest extends BaseCardTest {

    private void payAndCast(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.setHand(player, List.of(new DistantMelody()));
        harness.castSorcery(player, 0, 0);
        harness.passBothPriorities();
    }

    private void stockLibrary(Player player, int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new Shock());
        }
        harness.setLibrary(player, deck);
    }

    @Test
    @DisplayName("Draws a card for each permanent of the chosen type you control")
    void drawsPerChosenTypeCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, "BEAR");

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Choosing a type you control none of draws no cards")
    void chosenTypeYouControlNoneDrawsZero() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("A Changeling you control counts as the chosen type")
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player1, new AvianChangeling());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Only the caster's permanents of the chosen type are counted")
    void onlyControllerPermanentsCounted() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, "BEAR");

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
