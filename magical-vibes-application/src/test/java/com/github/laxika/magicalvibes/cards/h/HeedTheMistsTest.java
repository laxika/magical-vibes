package com.github.laxika.magicalvibes.cards.h;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.b.BruvacTheGrandiloquent;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.m.MinamoSightbender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({HeedTheMists.class, MinamoSightbender.class, GodsEyeGateToTheReikai.class})
class HeedTheMistsTest extends BaseCardTest {

    @Test
    @DisplayName("Mills the top card and draws cards equal to its mana value")
    void millsAndDrawsByManaValue() {
        prepare();
        harness.setLibrary(player1, List.of(
                new MinamoSightbender(),
                new GodsEyeGateToTheReikai(),
                new GodsEyeGateToTheReikai(),
                new GodsEyeGateToTheReikai()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        // Minamo Sightbender (mana value 2) is milled, then two cards are drawn.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2); // milled card + Heed the Mists
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1 + 2);
    }

    @Test
    @DisplayName("Draws nothing when the milled card has mana value zero")
    void noDrawForZeroManaValue() {
        prepare();
        harness.setLibrary(player1, List.of(
                new GodsEyeGateToTheReikai(),
                new GodsEyeGateToTheReikai(),
                new GodsEyeGateToTheReikai()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1);
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void emptyLibraryDoesNothing() {
        prepare();
        harness.setLibrary(player1, List.of());
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1);
    }

    @Test
    @DisplayName("Draws only cards available when the library is shorter than the mana value")
    void drawsOnlyAvailableCardsWhenLibraryIsShort() {
        prepare();
        harness.setLibrary(player1, List.of(
                new MinamoSightbender(),
                new GodsEyeGateToTheReikai()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @CardUsed(BruvacTheGrandiloquent.class)
    @DisplayName("Sums the mana values when a mill replacement mills two cards")
    void sumsManaValuesOfCardsMilledByReplacementEffect() {
        prepare();
        harness.addToBattlefield(player2, new BruvacTheGrandiloquent());
        harness.setLibrary(player1, List.of(
                new GodsEyeGateToTheReikai(),
                new MinamoSightbender(),
                new GodsEyeGateToTheReikai(),
                new GodsEyeGateToTheReikai()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3); // two milled cards + Heed the Mists
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1 + 2);
    }

    @Test
    @CardUsed(LeylineOfTheVoid.class)
    @DisplayName("Uses the mana value of a milled card exiled by a replacement effect")
    void drawsForMilledCardExiledByReplacementEffect() {
        prepare();
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        harness.setLibrary(player1, List.of(
                new MinamoSightbender(),
                new GodsEyeGateToTheReikai(),
                new GodsEyeGateToTheReikai()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1 + 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new HeedTheMists()));
        harness.addMana(player1, ManaColor.BLUE, 5);
    }
}
