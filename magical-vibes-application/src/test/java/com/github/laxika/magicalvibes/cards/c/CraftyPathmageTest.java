package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({CraftyPathmage.class, GiantGrowth.class, GrizzlyBears.class, CrawWurm.class, Island.class})
class CraftyPathmageTest extends BaseCardTest {

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent pathmage = addCreatureReady(player1, new CraftyPathmage());

        harness.activateAbility(player1, 0, null, pathmage.getId());
        harness.passBothPriorities();

        assertThat(pathmage.isTapped()).isTrue();
        assertThat(pathmage.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent pathmage = harness.addToBattlefieldAndReturn(player1, new CraftyPathmage());
        pathmage.setSummoningSick(true);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pathmage.isTapped()).isFalse();
        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent pathmage = addCreatureReady(player1, new CraftyPathmage());
        pathmage.setTapped(true);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Growing after resolution does not allow the creature to be blocked")
    void targetRemainsUnblockableAfterGrowing() {
        addCreatureReady(player1, new CraftyPathmage());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new CrawWurm());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(target.isCantBeBlocked()).isTrue();

        declareAttackersAndPrepareBlockers(player1, List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Makes an opponent's 2/2 creature unblockable until end of turn")
    void makesOpponentPowerTwoCreatureUnblockableUntilEndOfTurn() {
        Permanent pathmage = addCreatureReady(player1, new CraftyPathmage());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(pathmage.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Can target a creature its controller controls")
    void canTargetOwnCreature() {
        Permanent pathmage = addCreatureReady(player1, new CraftyPathmage());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(pathmage.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("A creature made unblockable cannot be declared as blocked")
    void preventsBlockingAnAffectedCreature() {
        addCreatureReady(player1, new CraftyPathmage());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetLargeCreature() {
        Permanent pathmage = addCreatureReady(player1, new CraftyPathmage());
        Permanent target = addCreatureReady(player2, new CrawWurm());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power must be 2 or less");
        assertThat(pathmage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent pathmage = addCreatureReady(player1, new CraftyPathmage());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pathmage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Fizzling target that grows above power 2 does not become unblockable")
    void targetThatGrowsBeforeResolutionBecomesIllegal() {
        Permanent pathmage = addCreatureReady(player1, new CraftyPathmage());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(pathmage.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(target.isCantBeBlocked()).isFalse();
    }
}
