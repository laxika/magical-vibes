package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.s.SleeperDart;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeightenedReflexes.class, AlmightyBrushwagg.class, SleeperDart.class})
class HeightenedReflexesTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +1/+0 and a first strike counter")
    void givesBoostAndFirstStrikeCounter() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new HeightenedReflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The power boost expires but the first strike counter remains")
    void boostExpiresButCounterRemains() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new HeightenedReflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SleeperDart());
        harness.setHand(player1, List.of(new HeightenedReflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can boost an opponent's creature and put the counter on it")
    void canTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new HeightenedReflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Repeated casts stack boosts and place additional first strike counters")
    void repeatedCastsAccumulateBoostsAndCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new HeightenedReflexes(), new HeightenedReflexes()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }
}
