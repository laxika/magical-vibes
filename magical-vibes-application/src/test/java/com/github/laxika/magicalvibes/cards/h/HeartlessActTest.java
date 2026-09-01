package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeartlessAct.class, GrizzlyBears.class})
class HeartlessActTest extends BaseCardTest {

    @Test
    void destroysCreatureWithNoCounters() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castHeartlessAct(0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotDestroyCreatureWithCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> castHeartlessAct(0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removesUpToThreeCountersFromTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        castHeartlessAct(1, target.getId());
        harness.handleListChoice(player1, "3");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castHeartlessAct(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new HeartlessAct()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }
}
