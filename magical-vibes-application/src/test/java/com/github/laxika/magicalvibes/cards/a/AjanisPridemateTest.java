package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

class AjanisPridemateTest extends BaseCardTest {

    

    @Test
    @DisplayName("Gets a +1/+1 counter when controller gains life from ETB effect")
    void getsCounterOnLifeGain() {
        harness.addToBattlefield(player1, new AjanisPridemate());

        Permanent pridemate = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        // Cast Angel of Mercy (ETB: gain 3 life) to trigger life gain
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB triggers)
        harness.passBothPriorities(); // resolve life gain triggered ability (GainLifeEffect)
        harness.passBothPriorities(); // resolve Pridemate's +1/+1 counter triggered ability

        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, pridemate)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pridemate)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not get counter when opponent gains life")
    void noCounterWhenOpponentGainsLife() {
        harness.addToBattlefield(player1, new AjanisPridemate());

        Permanent pridemate = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Opponent gains life
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB triggers)
        harness.passBothPriorities(); // resolve life gain triggered ability

        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple life gain events each trigger independently")
    void multipleLifeGainEventsEachTrigger() {
        harness.addToBattlefield(player1, new AjanisPridemate());
        harness.addToBattlefield(player1, new SoulWarden());

        Permanent pridemate = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ajani's Pridemate"))
                .findFirst().orElseThrow();

        // Cast a creature — Soul Warden triggers (gain 1 life), which triggers Pridemate
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (Soul Warden triggers)
        harness.passBothPriorities(); // resolve Soul Warden's GainLifeEffect
        harness.passBothPriorities(); // resolve Pridemate's +1/+1 counter

        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Pridemates each get a counter on life gain")
    void multiplePridematesEachGetCounter() {
        harness.addToBattlefield(player1, new AjanisPridemate());
        harness.addToBattlefield(player1, new AjanisPridemate());

        List<Permanent> pridemates = gd.playerBattlefields.get(player1.getId());
        Permanent pridemate1 = pridemates.get(0);
        Permanent pridemate2 = pridemates.get(1);

        // Cast Angel of Mercy to trigger life gain
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB triggers)
        harness.passBothPriorities(); // resolve life gain triggered ability
        harness.passBothPriorities(); // resolve first Pridemate's triggered ability
        harness.passBothPriorities(); // resolve second Pridemate's triggered ability

        assertThat(pridemate1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(pridemate2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
