package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlmsCollector.class, Forest.class, GrizzlyBears.class, Island.class})
class AlmsCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces an opponent's draw of two or more cards")
    void replacesOpponentMultiCardDraw() {
        harness.addToBattlefield(player1, new AlmsCollector());
        Card opponentFirst = new Forest();
        Card opponentSecond = new GrizzlyBears();
        Card controllerCard = new Island();
        harness.setLibrary(player2, List.of(opponentFirst, opponentSecond));
        harness.setLibrary(player1, List.of(controllerCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player2.getId(), 2));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentFirst);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentSecond);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not replace an opponent's single-card draw")
    void doesNotReplaceSingleCardDraw() {
        harness.addToBattlefield(player1, new AlmsCollector());
        Card opponentCard = new Forest();
        Card controllerCard = new Island();
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setLibrary(player1, List.of(controllerCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerCard);
    }

    @Test
    @DisplayName("Does not replace the controller's own multi-card draw")
    void doesNotReplaceControllerDraw() {
        harness.addToBattlefield(player1, new AlmsCollector());
        Card first = new Island();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
