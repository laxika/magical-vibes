package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DirtcowlWurm.class, Forest.class})
class DirtcowlWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent plays a land")
    void countersOnOpponentLandPlay() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new DirtcowlWurm());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger when its controller plays a land")
    void noCounterOnControllerLandPlay() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new DirtcowlWurm());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's land merely enters the battlefield")
    void noCounterWhenOpponentLandJustEnters() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new DirtcowlWurm());

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
