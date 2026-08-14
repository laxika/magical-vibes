package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScrawlingCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep, each player draws a card and each opponent draw costs 1 life")
    void eachPlayerDrawsAtControllerUpkeep() {
        harness.addToBattlefield(player1, new ScrawlingCrawler());
        setDeck(player1, List.of(new Forest()));
        setDeck(player2, List.of(new Forest()));
        harness.setLife(player2, 20);

        int player1HandSize = gd.playerHands.get(player1.getId()).size();
        int player2HandSize = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandSize + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Opponent loses 1 life for each card drawn")
    void opponentLosesLifeForEachCardDrawn() {
        harness.addToBattlefield(player1, new ScrawlingCrawler());
        setDeck(player2, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CounselOfTheSoratami()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Controller's draw does not trigger life loss")
    void controllerDrawDoesNotTriggerLifeLoss() {
        harness.addToBattlefield(player1, new ScrawlingCrawler());
        setDeck(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opponent's upkeep does not trigger the controller's draw ability")
    void opponentUpkeepDoesNotTriggerDrawAbility() {
        harness.addToBattlefield(player1, new ScrawlingCrawler());
        setDeck(player1, List.of(new Forest()));
        setDeck(player2, List.of(new Forest()));
        harness.setLife(player2, 20);

        int player1HandSize = gd.playerHands.get(player1.getId()).size();
        int player2HandSize = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandSize);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void setDeck(Player player, List<? extends Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
