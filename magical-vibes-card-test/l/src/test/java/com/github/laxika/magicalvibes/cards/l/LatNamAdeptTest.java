package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LatNamAdept.class})
class LatNamAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself for the second card drawn each turn")
    void triggersOnSecondCardDrawn() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new LatNamAdept());
        harness.setLibrary(player1, List.of(new LatNamAdept(), new LatNamAdept(), new LatNamAdept()));

        drawAndResolveTrigger(player1);
        assertThat(adept.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        drawAndResolveTrigger(player1);
        assertThat(adept.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        drawAndResolveTrigger(player1);
        assertThat(adept.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent drawing a card does not trigger Lat-Nam Adept")
    void opponentDrawDoesNotTrigger() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new LatNamAdept());
        harness.setLibrary(player2, List.of(new LatNamAdept()));

        drawAndResolveTrigger(player2);

        assertThat(adept.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        resolveAllTriggers();
    }

    @Test
    void opponentsSecondDrawDoesNotTrigger() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new LatNamAdept());
        harness.setLibrary(player2, List.of(new LatNamAdept(), new LatNamAdept()));

        drawAndResolveTrigger(player2);
        drawAndResolveTrigger(player2);

        assertThat(adept.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new LatNamAdept());
        harness.setLibrary(player1, List.of(new LatNamAdept(), new LatNamAdept()));

        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);

        assertThat(adept.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void countsDrawsBeforeEnteringBattlefield() {
        harness.setLibrary(player1, List.of(new LatNamAdept(), new LatNamAdept()));
        drawAndResolveTrigger(player1);
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new LatNamAdept());

        drawAndResolveTrigger(player1);

        assertThat(adept.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void eachAdeptGetsItsOwnCounterFromMultipleCardDraw() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LatNamAdept());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LatNamAdept());
        harness.setLibrary(player1, List.of(new LatNamAdept(), new LatNamAdept(), new LatNamAdept()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 3));
        assertThat(gd.stack).hasSize(2);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotPutCounterOnANewAdeptWhenSourceLeaves() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new LatNamAdept());
        harness.setLibrary(player1, List.of(new LatNamAdept(), new LatNamAdept()));
        drawAndResolveTrigger(player1);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, original.getCard());
        resolveAllTriggers();

        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
