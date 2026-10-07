package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CalciteSnapper;
import com.github.laxika.magicalvibes.cards.h.HalimarDepths;
import com.github.laxika.magicalvibes.cards.k.KhalniGarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreasureHunt.class, CalciteSnapper.class, HalimarDepths.class, KhalniGarden.class})
class TreasureHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Puts lands and the first nonland revealed into hand")
    void putsAllRevealedCardsIntoHand() {
        Card forest = new KhalniGarden();
        Card shock = new CalciteSnapper();
        Card islandBelow = new HalimarDepths();
        harness.setLibrary(player1, List.of(forest, shock, islandBelow));

        castTreasureHunt();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(islandBelow);
    }

    @Test
    @DisplayName("Puts the entire library into hand when no nonland is revealed")
    void putsEntireLibraryIntoHandWhenNoNonlandExists() {
        Card forest = new KhalniGarden();
        Card island = new HalimarDepths();
        harness.setLibrary(player1, List.of(forest, island));

        castTreasureHunt();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, island);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Stops immediately when the top card is nonland and does not draw cards")
    void stopsAtTopNonlandWithoutDrawing() {
        Card nonland = new CalciteSnapper();
        Card landBelow = new HalimarDepths();
        Card secondNonland = new TreasureHunt();
        Card opponentsCard = new KhalniGarden();
        harness.setLibrary(player1, List.of(nonland, landBelow, secondNonland));
        harness.setLibrary(player2, List.of(opponentsCard));
        int drawsBefore = gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0);

        castTreasureHunt();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(landBelow, secondNonland);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(drawsBefore);
    }

    @Test
    @DisplayName("Resolves harmlessly with an empty library")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castTreasureHunt();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof TreasureHunt);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    private void castTreasureHunt() {
        harness.setHand(player1, List.of(new TreasureHunt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
