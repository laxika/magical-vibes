package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({RaggedPlaymate.class, GrizzlyBears.class, HillGiant.class})
class RaggedPlaymateTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature with power 2 or less can't be blocked this turn")
    void makesPowerTwoCreatureUnblockable() {
        addCreatureReady(player1, new RaggedPlaymate());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new RaggedPlaymate());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetLargeCreature() {
        addCreatureReady(player1, new RaggedPlaymate());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");
    }

    @Test
    void canTargetItselfAndPaysTapCostBeforeResolution() {
        Permanent playmate = addCreatureReady(player1, new RaggedPlaymate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, playmate.getId());

        assertThat(playmate.isTapped()).isTrue();
        assertThat(playmate.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();
        assertThat(playmate.isCantBeBlocked()).isTrue();
    }

    @Test
    void canTargetOpponentsCreature() {
        addCreatureReady(player1, new RaggedPlaymate());
        Permanent target = addCreatureReady(player2, new RaggedPlaymate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void doesNotResolveWhenTargetPowerIncreasesAboveTwo() {
        addCreatureReady(player1, new RaggedPlaymate());
        Permanent target = addCreatureReady(player1, new RaggedPlaymate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void effectRemainsAfterPowerIncreasesFollowingResolution() {
        addCreatureReady(player1, new RaggedPlaymate());
        Permanent target = addCreatureReady(player1, new RaggedPlaymate());
        addCreatureReady(player2, new RaggedPlaymate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        target.setPowerModifier(1);
        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent playmate = addCreatureReady(player1, new RaggedPlaymate());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, playmate.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(playmate.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent playmate = harness.addToBattlefieldAndReturn(player1, new RaggedPlaymate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, playmate.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(playmate.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}