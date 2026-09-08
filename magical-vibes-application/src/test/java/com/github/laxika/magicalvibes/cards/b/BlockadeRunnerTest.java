package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlockadeRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability makes Blockade Runner unblockable this turn")
    void abilityMakesSelfUnblockable() {
        Permanent runner = addRunner();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(runner.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off during cleanup")
    void unblockableWearsOff() {
        Permanent runner = addRunner();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(runner.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(runner.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Ability cannot be activated without blue mana")
    void abilityRequiresMana() {
        Permanent runner = addRunner();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(runner.isCantBeBlocked()).isFalse();
    }

    private Permanent addRunner() {
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new BlockadeRunner());
        runner.setSummoningSick(false);
        return runner;
    }
}
