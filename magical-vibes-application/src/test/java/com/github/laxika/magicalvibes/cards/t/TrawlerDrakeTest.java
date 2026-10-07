package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrawlerDrake.class, Shock.class, GrizzlyBears.class, PropheticPrism.class})
class TrawlerDrakeTest extends BaseCardTest {

    private Permanent addDrake() {
        harness.castFromHand(player1, new TrawlerDrake(), "{2}{U}");
        harness.passBothPriorities();
        return findPermanent(player1, "Trawler Drake");
    }

    @Test
    @DisplayName("Enters with an oil counter and gets +1/+1 for each oil counter")
    void entersWithOilCounterAndScalesWithOilCounters() {
        Permanent drake = addDrake();
        assertThat(drake.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(1);

        drake.setCounterCount(CounterType.OIL, 3);

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets an oil counter when its controller casts a noncreature spell")
    void noncreatureSpellAddsOilCounter() {
        Permanent drake = addDrake();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(drake.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get an oil counter when its controller casts a creature spell")
    void creatureSpellDoesNotAddOilCounter() {
        Permanent drake = addDrake();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(drake.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a noncreature spell")
    void opponentNoncreatureSpellDoesNotAddOilCounter() {
        Permanent drake = addDrake();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(drake.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("A noncreature artifact spell adds oil before the artifact resolves")
    void noncreaturePermanentSpellAddsOilCounter() {
        Permanent drake = addDrake();

        harness.castFromHand(player1, new PropheticPrism(), "{2}");

        assertThat(drake.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(drake.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Prophetic Prism");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Every noncreature spell cast adds another oil counter")
    void multipleNoncreatureSpellsEachAddOilCounter() {
        Permanent drake = addDrake();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(drake.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(3);
    }

    @Test
    @DisplayName("Losing all oil counters makes the Drake die from zero toughness")
    void losingAllOilCountersCausesDeath() {
        Permanent drake = addDrake();
        drake.setCounterCount(CounterType.OIL, 0);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Trawler Drake");
        harness.assertInGraveyard(player1, "Trawler Drake");
    }

    @Test
    @DisplayName("Other counter types do not increase the oil-based bonus")
    void unrelatedCountersDoNotIncreaseBonus() {
        Permanent drake = addDrake();
        drake.setCounterCount(CounterType.CHARGE, 3);

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(1);
    }
}
