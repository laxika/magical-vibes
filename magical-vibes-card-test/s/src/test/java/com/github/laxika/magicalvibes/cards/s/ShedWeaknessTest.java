package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShedWeakness.class, AirElemental.class, GrizzlyBears.class, Forest.class})
class ShedWeaknessTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts +2/+2 and, when accepted, removes exactly one -1/-1 counter from the target")
    void acceptRemovesOneCounter() {
        // Air Elemental (4/4) carrying two -1/-1 counters → 2/2.
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        castTargeting(creature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // One counter removed (not all), plus +2/+2: 4 base + 2 boost - 1 counter = 5/5.
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining the counter removal still applies the +2/+2 boost")
    void declineKeepsCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        castTargeting(creature);
        harness.handleMayAbilityChosen(player1, false);

        // Counters untouched, boost applied: 4 base + 2 boost - 2 counters = 4/4.
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The +2/+2 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castTargeting(creature);
        harness.handleMayAbilityChosen(player1, true); // no counters — removal is a no-op

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // a legal target exists
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ShedWeakness()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID landId = land.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The caster may remove a counter from an opponent's creature without affecting another creature")
    void canRemoveCounterFromOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 1);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        other.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        castTargeting(target);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(other.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(other.getEffectivePower()).isEqualTo(3);
        assertThat(other.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A target that leaves before resolution prevents both the boost and the optional removal")
    void removedTargetDoesNotResolve() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new ShedWeakness()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Shed Weakness");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    private void castTargeting(Permanent target) {
        harness.setHand(player1, List.of(new ShedWeakness()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
