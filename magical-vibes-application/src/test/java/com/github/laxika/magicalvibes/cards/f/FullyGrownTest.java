package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FullyGrown.class, GrizzlyBears.class, FountainOfYouth.class})
class FullyGrownTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +3/+3 and a trample counter")
    void givesBoostAndTrampleCounter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FullyGrown()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(3);
        assertThat(bear.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The power and toughness boost expires but the trample counter remains")
    void boostExpiresButCounterRemains() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FullyGrown()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new FullyGrown()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
