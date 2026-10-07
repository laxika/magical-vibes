package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.u.UbaMask;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritOfTheLabyrinth.class, Forest.class, Island.class, UbaMask.class, TurnToFrog.class, Divination.class})
class SpiritOfTheLabyrinthTest extends BaseCardTest {

    @Test
    @DisplayName("A player can draw only one card each turn")
    void limitsDrawsPerPlayerPerTurn() {
        harness.addToBattlefield(player1, new SpiritOfTheLabyrinth());
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));

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
        harness.setLibrary(player1, List.of(player1First, player1Second));
        harness.setLibrary(player2, List.of(player2First, player2Second));

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
        harness.setLibrary(player1, List.of(first, second));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("A draw made before Spirit enters still counts toward the limit")
    void countsDrawsBeforeEntering() {
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.addToBattlefield(player2, new SpiritOfTheLabyrinth());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("A prohibited draw cannot be replaced by Uba Mask")
    void blocksReplacementAfterARealDraw() {
        harness.addToBattlefield(player1, new SpiritOfTheLabyrinth());
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.addToBattlefield(player1, new UbaMask());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Removing Spirit's abilities allows additional draws")
    void abilityRemovalDisablesDrawLimit() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new SpiritOfTheLabyrinth());
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, spirit.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("A spell instructing a player to draw two cards draws only one")
    void limitsMultiCardDrawSpell() {
        harness.addToBattlefield(player2, new SpiritOfTheLabyrinth());
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("The draw allowance resets on the opponent's turn")
    void allowsAnotherDrawOnOpponentsTurn() {
        harness.addToBattlefield(player1, new SpiritOfTheLabyrinth());
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }
}
