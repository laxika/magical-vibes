package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BlockadeRunner.class)
class BlockadeRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability makes Blockade Runner unblockable this turn")
    void abilityMakesSelfUnblockable() {
        Permanent runner = addCreatureReady(player1, new BlockadeRunner());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(runner.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off during cleanup")
    void unblockableWearsOff() {
        Permanent runner = addCreatureReady(player1, new BlockadeRunner());
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
        Permanent runner = addCreatureReady(player1, new BlockadeRunner());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(runner.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Ability can be activated while Blockade Runner has summoning sickness")
    void abilityDoesNotRequireCreatureToBeReady() {
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new BlockadeRunner());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(runner.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("An unblockable Blockade Runner cannot be assigned a blocker")
    void cannotBeBlockedInCombat() {
        Permanent runner = addCreatureReady(player1, new BlockadeRunner());
        Permanent blocker = addCreatureReady(player2, new BlockadeRunner());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        runner.setAttacking(true);
        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
