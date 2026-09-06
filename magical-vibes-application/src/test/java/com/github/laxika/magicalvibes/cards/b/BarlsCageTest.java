package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarlsCage.class, FountainOfYouth.class, GrizzlyBears.class})
class BarlsCageTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingTargetingCreaturePutsOnStack() {
        addReadyCage(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Can activate while already tapped because tapping is not part of the cost")
    void canActivateWithTappedSource() {
        Permanent cage = addReadyCage(player1);
        cage.tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(cage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving makes target skip its next untap step")
    void resolvingSkipsNextUntap() {
        addReadyCage(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not tap the target creature")
    void doesNotTapTarget() {
        addReadyCage(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Target remains tapped during its next controller untap step")
    void targetRemainsTappedThroughNextUntapStep() {
        addReadyCage(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreaturePermanent() {
        addReadyCage(player1);
        Permanent nonCreature = addReadyNonCreaturePermanent(player2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyCage(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Does not lock a target that leaves before resolution")
    void doesNotLockTargetThatLeavesBeforeResolution() {
        addReadyCage(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(harness.getGameData(), target));
        harness.passBothPriorities();
        assertThat(target.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Resolves even if the source leaves before resolution")
    void resolvesAfterSourceLeavesBeforeResolution() {
        Permanent cage = addReadyCage(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(harness.getGameData(), cage));

        harness.passBothPriorities();

        assertThat(target.getSkipUntapCount()).isEqualTo(1);
    }

    private Permanent addReadyCage(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BarlsCage());
    }

    private Permanent addReadyNonCreaturePermanent(Player player) {
        return harness.addToBattlefieldAndReturn(player, new FountainOfYouth());
    }
}
