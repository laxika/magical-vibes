package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SorceressQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Ability sets target creature's base power and toughness to 0/2")
    void setsTargetBasePowerToughness() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new SorceressQueen());
        queen.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.isBasePowerToughnessOverriddenUntilEndOfTurn()).isTrue();
        assertThat(bear.getEffectivePower()).isEqualTo(0);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Base power/toughness override wears off at cleanup")
    void wearsOffAtCleanup() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new SorceressQueen());
        queen.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.isBasePowerToughnessOverriddenUntilEndOfTurn()).isFalse();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target itself with the ability")
    void cannotTargetItself() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new SorceressQueen());
        queen.setSummoningSick(false);
        // Another legal creature target so the ability is activatable at all.
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, queen.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }
}
