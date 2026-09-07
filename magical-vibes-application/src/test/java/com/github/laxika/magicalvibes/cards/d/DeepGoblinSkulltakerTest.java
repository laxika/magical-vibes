package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepGoblinSkulltaker.class, ZuranOrb.class, Forest.class})
class DeepGoblinSkulltakerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself at your end step after descending")
    void putsCounterAfterDescending() {
        Permanent skulltaker = harness.addToBattlefieldAndReturn(player1, new DeepGoblinSkulltaker());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(skulltaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself at your end step without descending")
    void doesNotPutCounterWithoutDescending() {
        Permanent skulltaker = harness.addToBattlefieldAndReturn(player1, new DeepGoblinSkulltaker());

        advanceToEndStep(player1);

        assertThat(skulltaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
