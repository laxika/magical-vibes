package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Fireslinger;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SegmentedWurm.class, SearingTouch.class, Fireslinger.class, TrainedArmodon.class,
        SpellBlast.class})
class SegmentedWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming the target of a spell puts a -1/-1 counter on Segmented Wurm")
    void targetedBySpellGetsCounter() {
        Permanent wurm = addCreatureReady(player2, new SegmentedWurm());

        harness.setHand(player1, List.of(new SearingTouch()));
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
        Permanent wurm = addCreatureReady(player2, new SegmentedWurm());

        Permanent fireslinger = addCreatureReady(player1, new Fireslinger());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(fireslinger), null,
                wurm.getId());

        harness.passBothPriorities(); // resolve the becomes-target trigger

        assertThat(wurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell targeting another creature does not put a counter on Segmented Wurm")
    void untargetedWurmKeepsSize() {
        Permanent wurm = addCreatureReady(player2, new SegmentedWurm());

        Permanent armodon = addCreatureReady(player2, new TrainedArmodon());

        harness.setHand(player1, List.of(new SearingTouch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, armodon.getId());
        harness.passBothPriorities();

        assertThat(wurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(wurm.getEffectivePower()).isEqualTo(5);
    }

    @Test
    @DisplayName("The trigger resolves even if the targeting spell is countered")
    void counteredTargetingSpellStillPutsCounterOnWurm() {
        Permanent wurm = addCreatureReady(player2, new SegmentedWurm());
        SearingTouch searingTouch = new SearingTouch();

        harness.setHand(player1, List.of(searingTouch));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new SpellBlast()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, searingTouch.getId());
        resolveAllTriggers();

        assertThat(wurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Searing Touch");
    }
}
