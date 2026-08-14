package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpinalParasiteTest extends BaseCardTest {

    @Test
    @DisplayName("Sunburst puts one +1/+1 counter on Spinal Parasite for each color spent")
    void sunburstCountsDistinctColors() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpinalParasite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent parasite = findPermanent(player1, "Spinal Parasite");
        assertThat(parasite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Activated ability removes two +1/+1 counters and a counter from the target")
    void removesCounters() {
        Permanent parasite = addReadyParasite();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(parasite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Activated ability requires two +1/+1 counters")
    void requiresTwoPlusOneCounters() {
        Permanent parasite = addReadyParasite();
        parasite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    private Permanent addReadyParasite() {
        Permanent parasite = new Permanent(new SpinalParasite());
        parasite.setSummoningSick(false);
        parasite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.playerBattlefields.get(player1.getId()).add(parasite);
        return parasite;
    }
}
