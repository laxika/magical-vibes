package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchangelOfThune.class, AngelOfMercy.class, GrizzlyBears.class, SoulWarden.class})
class ArchangelOfThuneTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature you control when you gain life")
    void countersOnLifeGain() {
        harness.addToBattlefield(player1, new ArchangelOfThune());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent archangel = findPermanent(player1, "Archangel of Thune");

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(archangel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player2, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void noTriggerOnOpponentLifeGain() {
        harness.addToBattlefield(player1, new ArchangelOfThune());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each separate life gain event triggers again")
    void triggersPerLifeGainEvent() {
        harness.addToBattlefield(player1, new ArchangelOfThune());
        harness.addToBattlefield(player1, new SoulWarden());

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        // The Archangel was on the battlefield for both life gain events.
        assertThat(findPermanent(player1, "Archangel of Thune")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Its own lifelink damage gains life and triggers one counter per creature")
    void lifelinkTriggersCounters() {
        Permanent archangel = addCreatureReady(player1, new ArchangelOfThune());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        archangel.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        assertThat(archangel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();
        assertThat(archangel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Archangel triggers independently for the same life gain")
    void multipleArchangelsTriggerIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArchangelOfThune());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ArchangelOfThune());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Angel of Mercy")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
