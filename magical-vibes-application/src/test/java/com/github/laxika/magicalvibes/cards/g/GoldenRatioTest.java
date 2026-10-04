package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldenRatio.class, GrizzlyBears.class, LlanowarElves.class, ShivanDragon.class,
        Shock.class, GiantGrowth.class})
class GoldenRatioTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card for each different power among your creatures")
    void drawsForEachDifferentControlledCreaturePower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new ShivanDragon());
        harness.addToBattlefield(player2, new ShivanDragon());
        stockLibrary(player1, 6);

        castGoldenRatio(player1);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Draws no cards when you control no creatures")
    void drawsNoCardsWithoutControlledCreatures() {
        stockLibrary(player1, 3);

        castGoldenRatio(player1);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    private void castGoldenRatio(Player player) {
        harness.castFromHand(player, new GoldenRatio(), "{1}{G}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Opposing creatures with unique powers do not increase the draw count")
    void ignoresUniqueOpposingCreaturePowers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new ShivanDragon());
        stockLibrary(player1, 4);

        castGoldenRatio(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A power increase in response can separate previously equal powers")
    void countsModifiedPowersAtResolution() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        stockLibrary(player1, 4);
        harness.castFromHand(player1, new GoldenRatio(), "{1}{G}{U}");
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A power increase in response can merge previously different powers")
    void countsEqualModifiedPowersOnlyOnce() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ShivanDragon());
        stockLibrary(player1, 4);
        harness.castFromHand(player1, new GoldenRatio(), "{1}{G}{U}");
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    private void stockLibrary(Player player, int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new Shock());
        }
        harness.setLibrary(player, deck);
    }
}
