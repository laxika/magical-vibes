package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChariotOfTheSunTest extends BaseCardTest {

    @Test
    @DisplayName("Grants flying and sets base toughness to 1 without touching base power")
    void grantsFlyingAndSetsBaseToughness() {
        harness.addToBattlefield(player1, new ChariotOfTheSun());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters apply on top of the new base toughness")
    void countersApplyOnTopOfNewBaseToughness() {
        harness.addToBattlefield(player1, new ChariotOfTheSun());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        // Base 2/1 after the ability, counters still apply on top: 4/3
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flying and the base toughness set both wear off at cleanup")
    void wearsOffAtCleanup() {
        harness.addToBattlefield(player1, new ChariotOfTheSun());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new ChariotOfTheSun());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID opponentBearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
