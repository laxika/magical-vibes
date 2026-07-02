package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ComfortingCounselTest extends BaseCardTest {

    @Test
    @DisplayName("Has life-gain trigger and counter-threshold anthem static effect")
    void hasCorrectEffects() {
        ComfortingCounsel card = new ComfortingCounsel();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_GAINS_LIFE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_GAINS_LIFE).getFirst())
                .isInstanceOf(PutCountersOnSelfEffect.class);
        PutCountersOnSelfEffect counterEffect =
                (PutCountersOnSelfEffect) card.getEffects(EffectSlot.ON_CONTROLLER_GAINS_LIFE).getFirst();
        assertThat(counterEffect.counterType()).isEqualTo(CounterType.GROWTH);
        assertThat(counterEffect.count()).isEqualTo(1);

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ConditionalEffect.class);
        ConditionalEffect conditional =
                (ConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(((SourceCounterThreshold) conditional.condition()).threshold()).isEqualTo(5);
        assertThat(((SourceCounterThreshold) conditional.condition()).counterType()).isEqualTo(CounterType.GROWTH);
        assertThat(conditional.wrapped()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect boost = (StaticBoostEffect) conditional.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(3);
        assertThat(boost.toughnessBoost()).isEqualTo(3);
        assertThat(boost.scope()).isEqualTo(GrantScope.OWN_CREATURES);
    }

    @Test
    @DisplayName("Puts a growth counter on itself when controller gains life")
    void putsGrowthCounterOnLifeGain() {
        harness.addToBattlefield(player1, new ComfortingCounsel());

        Permanent counsel = findPermanent(player1, "Comforting Counsel");
        assertThat(counsel.getCounterCount(CounterType.GROWTH)).isZero();

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Angel of Mercy (ETB gain 3 life)
        harness.passBothPriorities(); // resolve GainLifeEffect
        harness.passBothPriorities(); // resolve Comforting Counsel's triggered ability

        assertThat(counsel.getCounterCount(CounterType.GROWTH)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put growth counters when only the opponent gains life")
    void noCounterWhenOpponentGainsLife() {
        harness.addToBattlefield(player1, new ComfortingCounsel());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve Angel of Mercy
        harness.passBothPriorities(); // resolve GainLifeEffect

        Permanent counsel = findPermanent(player1, "Comforting Counsel");
        assertThat(counsel.getCounterCount(CounterType.GROWTH)).isZero();
    }

    @Test
    @DisplayName("No creature boost with fewer than five growth counters")
    void noBoostBelowThreshold() {
        harness.addToBattlefield(player1, new ComfortingCounsel());
        Permanent counsel = findPermanent(player1, "Comforting Counsel");
        counsel.setCounterCount(CounterType.GROWTH, 4);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Grants +3/+3 to creatures you control at five or more growth counters")
    void grantsBoostAtThreshold() {
        harness.addToBattlefield(player1, new ComfortingCounsel());
        Permanent counsel = findPermanent(player1, "Comforting Counsel");
        counsel.setCounterCount(CounterType.GROWTH, 5);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new ComfortingCounsel());
        findPermanent(player1, "Comforting Counsel").setCounterCount(CounterType.GROWTH, 5);

        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains and loses the anthem as growth counters cross the threshold")
    void boostIsDynamic() {
        harness.addToBattlefield(player1, new ComfortingCounsel());
        Permanent counsel = findPermanent(player1, "Comforting Counsel");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        counsel.setCounterCount(CounterType.GROWTH, 4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        counsel.setCounterCount(CounterType.GROWTH, 5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);

        counsel.setCounterCount(CounterType.GROWTH, 3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
