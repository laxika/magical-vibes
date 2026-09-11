package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({InfuseWithTheElements.class, GrizzlyBears.class})
class InfuseWithTheElementsTest extends BaseCardTest {

    @Test
    void putsOneCounterWhenOneColorIsSpent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castWithMana(creature, ManaColor.GREEN, 4);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void putsTwoCountersWhenTwoColorsAreSpent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new InfuseWithTheElements()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void trampleWearsOffAtEndOfTurnButCountersRemain() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castWithMana(creature, ManaColor.GREEN, 4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
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

    private void castWithMana(Permanent creature, ManaColor color, int amount) {
        harness.setHand(player1, List.of(new InfuseWithTheElements()));
        harness.addMana(player1, color, amount);
        harness.castAndResolveInstant(player1, 0, creature.getId());
    }
}
