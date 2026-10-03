package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladeOfSharedSouls.class, GrizzlyBears.class, HillGiant.class})
class BladeOfSharedSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("For Mirrodin! creates a Rebel and its attachment trigger can copy another creature")
    void livingWeaponAndCopyTrigger() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new BladeOfSharedSouls()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent blade = findPermanent(player1, "Blade of Shared Souls");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(rebel.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipping Blade of Shared Souls triggers the copy choice")
    void equipTriggersCopyChoice() {
        harness.addToBattlefield(player1, new BladeOfSharedSouls());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, equipped.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(equipped.getCard().getName()).isEqualTo("Hill Giant");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(equipped.getCard().getName()).isEqualTo("Grizzly Bears");
        harness.handlePermanentChosen(player1, equipped.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void equippingSameCreatureDoesNotEndCopy() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BladeOfSharedSouls());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new HillGiant());
        equipAndCopy(0, equipped, target);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, equipped.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(equipped.getId());
        assertThat(equipped.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void detachingOlderBladePreservesNewerCopy() {
        assertRemainingBladeCopyAfterMoving(0);
    }

    @Test
    void detachingNewerBladeRestoresOlderCopy() {
        assertRemainingBladeCopyAfterMoving(1);
    }

    @Test
    void decliningCopyKeepsCreatureUnchangedAndEquipmentAttached() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BladeOfSharedSouls());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, equipped.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(blade.getAttachedTo()).isEqualTo(equipped.getId());
        assertThat(equipped.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void forMirrodinCreatesRebelWithoutAnotherControlledCreature() {
        addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new BladeOfSharedSouls()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rebel = findPermanent(player1, "Rebel");
        assertThat(findPermanent(player1, "Blade of Shared Souls").getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void assertRemainingBladeCopyAfterMoving(int movingBladeIndex) {
        Permanent older = harness.addToBattlefieldAndReturn(player1, new BladeOfSharedSouls());
        Permanent newer = harness.addToBattlefieldAndReturn(player1, new BladeOfSharedSouls());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        equipAndCopy(0, equipped, giant);
        equipAndCopy(1, equipped, giant);
        assertThat(equipped.getCard().getName()).isEqualTo("Hill Giant");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, movingBladeIndex, null, giant.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent remaining = movingBladeIndex == 0 ? newer : older;
        assertThat(remaining.getAttachedTo()).isEqualTo(equipped.getId());
        assertThat(equipped.getCard().getName())
                .isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, equipped)).isEqualTo(3);
    }

    private void equipAndCopy(int bladeIndex, Permanent equipped, Permanent target) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, bladeIndex, null, equipped.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }
}