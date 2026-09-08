package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TreasureHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Puts lands and the first nonland revealed into hand")
    void putsAllRevealedCardsIntoHand() {
        Card forest = new Forest();
        Card shock = new Shock();
        Card islandBelow = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest, shock, islandBelow));

        castTreasureHunt();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(islandBelow);
    }

    @Test
    @DisplayName("Puts the entire library into hand when no nonland is revealed")
    void putsEntireLibraryIntoHandWhenNoNonlandExists() {
        Card forest = new Forest();
        Card island = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest, island));

        castTreasureHunt();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, island);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castTreasureHunt() {
        harness.setHand(player1, List.of(new TreasureHunt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
