package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.t.TeferiMasterOfTime;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HistorianOfZhalfir.class, TeferiMasterOfTime.class, ChandraNalaar.class})
class HistorianOfZhalfirTest extends BaseCardTest {

    @Test
    void attackingWithTeferiPlaneswalkerDraws() {
        harness.addToBattlefieldAndReturn(player1, new TeferiMasterOfTime())
                .setCounterCount(CounterType.LOYALTY, 3);
        addCreatureReady(player1, new HistorianOfZhalfir());
        setDeck(player1, List.of(new ChandraNalaar()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void doesNotDrawWithoutControlledTeferiPlaneswalker() {
        addCreatureReady(player1, new HistorianOfZhalfir());
        setDeck(player1, List.of(new ChandraNalaar()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void conditionIsCheckedAgainWhenTriggerResolves() {
        harness.addToBattlefieldAndReturn(player1, new TeferiMasterOfTime())
                .setCounterCount(CounterType.LOYALTY, 3);
        addCreatureReady(player1, new HistorianOfZhalfir());
        setDeck(player1, List.of(new ChandraNalaar()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, 1);
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof TeferiMasterOfTime);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void declareAttackers(Player player, int historianIndex) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, List.of(historianIndex));
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
