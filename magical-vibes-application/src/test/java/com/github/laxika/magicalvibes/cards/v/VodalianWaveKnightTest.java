package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VodalianWaveKnight.class, MerfolkOfThePearlTrident.class, WhiteKnight.class, GrizzlyBears.class})
class VodalianWaveKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card counters other Merfolk and Knights you control")
    void drawCountersOtherMerfolkAndKnights() {
        Permanent waveKnight = addCreatureReady(player1, new VodalianWaveKnight());
        Permanent merfolk = addCreatureReady(player1, new MerfolkOfThePearlTrident());
        Permanent knight = addCreatureReady(player1, new WhiteKnight());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentMerfolk = addCreatureReady(player2, new MerfolkOfThePearlTrident());

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(waveKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentMerfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent drawing a card does not trigger Vodalian Wave-Knight")
    void opponentDrawDoesNotTrigger() {
        Permanent waveKnight = addCreatureReady(player1, new VodalianWaveKnight());
        Permanent merfolk = addCreatureReady(player1, new MerfolkOfThePearlTrident());

        advanceToDraw(player2);

        assertThat(waveKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }
}
