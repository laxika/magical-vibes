package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BroodhunterWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfuseWithTheElements.class, BroodhunterWurm.class})
class InfuseWithTheElementsTest extends BaseCardTest {

    @Test
    void putsOneCounterWhenOneColorIsSpent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BroodhunterWurm());
        castWithMana(creature, ManaColor.GREEN, 4);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void putsTwoCountersWhenTwoColorsAreSpent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BroodhunterWurm());
        harness.setHand(player1, List.of(new InfuseWithTheElements()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void trampleWearsOffAtEndOfTurnButCountersRemain() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BroodhunterWurm());
        castWithMana(creature, ManaColor.GREEN, 4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new InfuseWithTheElements()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void putsFourCountersWhenFourColorsAreSpent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BroodhunterWurm());
        harness.setHand(player1, List.of(new InfuseWithTheElements()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void colorlessManaDoesNotIncreaseConverge() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BroodhunterWurm());
        harness.setHand(player1, List.of(new InfuseWithTheElements()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void canTargetOpponentsCreatureWithoutAffectingOtherCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BroodhunterWurm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());

        castWithMana(target, ManaColor.GREEN, 4);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private void castWithMana(Permanent creature, ManaColor color, int amount) {
        harness.setHand(player1, List.of(new InfuseWithTheElements()));
        harness.addMana(player1, color, amount);
        harness.castAndResolveInstant(player1, 0, creature.getId());
    }
}
