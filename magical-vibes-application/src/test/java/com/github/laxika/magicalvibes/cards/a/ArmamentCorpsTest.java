package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class ArmamentCorpsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts both +1/+1 counters on one target creature you control")
    void putsBothCountersOnOneTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castArmamentCorps(List.of(target.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB distributes one +1/+1 counter to each of two target creatures you control")
    void distributesCountersAmongTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        castArmamentCorps(List.of(first.getId(), second.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArmamentCorps()));
        addArmamentCorpsMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private void castArmamentCorps(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new ArmamentCorps()));
        addArmamentCorpsMana();
        harness.castCreature(player1, 0, targetIds);
    }

    private void addArmamentCorpsMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
