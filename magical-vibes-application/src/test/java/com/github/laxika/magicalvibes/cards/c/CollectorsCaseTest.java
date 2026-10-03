package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CollectorsCase.class, GrizzlyBears.class, Millstone.class})
class CollectorsCaseTest extends BaseCardTest {

    @Test
    void entersByTappingTargetAndPuttingTwoStunCountersOnIt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    void mayEnterWithoutTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of());

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void alreadyTappedCreatureStillGetsTwoStunCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();

        cast(List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    void canTargetControllersOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    void stunCountersReplaceTheNextTwoUntaps() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(creature.getId()));

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void mayEnterWhenThereAreNoCreatures() {
        cast(List.of());

        harness.assertOnBattlefield(player1, "Collector's Case");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activatedAbilityTapsTargetCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CollectorsCase());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void activatedAbilityCannotTargetNoncreaturePermanent() {
        harness.addToBattlefieldAndReturn(player1, new CollectorsCase());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedSourceCannotActivateAbility() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CollectorsCase());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        source.tap();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void activatedAbilityRequiresFullManaCost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CollectorsCase());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new CollectorsCase()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, targetIds.isEmpty() ? null : targetIds.getFirst());
        resolveAllTriggers();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
