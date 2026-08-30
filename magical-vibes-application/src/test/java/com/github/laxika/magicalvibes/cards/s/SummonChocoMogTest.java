package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonChocoMog.class, GrizzlyBears.class})
class SummonChocoMogTest extends BaseCardTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void everyChapterBoostsOtherCreatures(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonChocoMog());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, saga)).isEqualTo(3);
        if (loreCounters == 3) {
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        } else {
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        }
    }

    @Test
    void stampedeExpiresAtEndOfTurn() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonChocoMog());
        saga.setCounterCount(CounterType.LORE, 0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
