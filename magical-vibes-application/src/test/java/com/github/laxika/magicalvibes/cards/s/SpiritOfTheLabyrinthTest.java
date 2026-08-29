package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.u.UbaMask;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpiritOfTheLabyrinthTest extends BaseCardTest {

    @Test
    @DisplayName("A player can draw only one card each turn")
    void limitsDrawsPerPlayerPerTurn() {
        harness.addToBattlefield(player1, new SpiritOfTheLabyrinth());
        Card first = new Forest();
        Card second = new Island();
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(first, second)));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });

        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The draw limit is tracked separately for each player")
    void tracksEachPlayerSeparately() {
        harness.addToBattlefield(player1, new SpiritOfTheLabyrinth());
        Card player1First = new Forest();
        Card player1Second = new Island();
        Card player2First = new Forest();
        Card player2Second = new Island();
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(player1First, player1Second)));
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(player2First, player2Second)));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
        });

        assertThat(gd.playerHands.get(player1.getId())).contains(player1First).doesNotContain(player1Second);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2First).doesNotContain(player2Second);
    }

    @Test
    @DisplayName("A draw replacement can still replace draws that do not count as draws")
    void doesNotBlockDrawReplacementBeforeARealDraw() {
        harness.addToBattlefield(player1, new SpiritOfTheLabyrinth());
        harness.addToBattlefield(player1, new UbaMask());
        Card first = new Forest();
        Card second = new Island();
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(first, second)));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }
}
