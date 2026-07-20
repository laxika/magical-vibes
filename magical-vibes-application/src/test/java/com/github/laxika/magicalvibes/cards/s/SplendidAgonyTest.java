package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SplendidAgonyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts both -1/-1 counters on a single target creature")
    void putsBothCountersOnOneTarget() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, List.of(giant.getId()));
        harness.passBothPriorities();

        // Hill Giant (3/3) with two -1/-1 counters → 1/1.
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(giant.getEffectivePower()).isEqualTo(1);
        assertThat(giant.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Distributes one -1/-1 counter on each of two target creatures")
    void distributesOneCounterEachAmongTwoTargets() {
        Permanent giant1 = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent giant2 = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, List.of(giant1.getId(), giant2.getId()));
        harness.passBothPriorities();

        // Each Hill Giant (3/3) with one -1/-1 counter → 2/2.
        assertThat(giant1.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(giant1.getEffectiveToughness()).isEqualTo(2);
        assertThat(giant2.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(giant2.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        // A legal creature target must exist so the spell is castable (CR 601.2c); the artifact is
        // still rejected as an illegal target by the per-target creature filter.
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
