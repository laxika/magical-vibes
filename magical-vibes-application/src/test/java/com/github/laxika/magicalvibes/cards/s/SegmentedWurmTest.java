package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SegmentedWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming the target of a spell puts a -1/-1 counter on Segmented Wurm")
    void targetedBySpellGetsCounter() {
        Permanent wurm = new Permanent(new SegmentedWurm());
        wurm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wurm);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, wurm.getId());

        harness.passBothPriorities(); // resolve the becomes-target trigger

        assertThat(wurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(wurm.getEffectivePower()).isEqualTo(4);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Becoming the target of an activated ability puts a -1/-1 counter on Segmented Wurm")
    void targetedByAbilityGetsCounter() {
        Permanent wurm = new Permanent(new SegmentedWurm());
        wurm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wurm);

        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent icy = findPermanent(player1, "Icy Manipulator");
        icy.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(icy), null, wurm.getId());

        harness.passBothPriorities(); // resolve the becomes-target trigger

        assertThat(wurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell targeting another creature does not put a counter on Segmented Wurm")
    void untargetedWurmKeepsSize() {
        Permanent wurm = new Permanent(new SegmentedWurm());
        wurm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wurm);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(wurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(wurm.getEffectivePower()).isEqualTo(5);
    }
}
