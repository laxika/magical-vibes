package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Aurochs;
import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
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

@CardUsed({RunedArch.class, Aurochs.class, BalduvianBarbarians.class, KjeldoranWarrior.class, GiantGrowth.class})
class RunedArchTest extends BaseCardTest {

    @Test
    @DisplayName("Runed Arch enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new RunedArch()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent arch = findPermanent(player1, "Runed Arch");
        assertThat(arch.isTapped()).isTrue();
    }

    @Test
    @DisplayName("X=2 makes two target creatures with power 2 or less unblockable and sacrifices the Arch")
    void makesXTargetsUnblockable() {
        harness.addToBattlefield(player1, new RunedArch());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Aurochs());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Aurochs());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isCantBeBlocked()).isTrue();
        assertThat(second.isCantBeBlocked()).isTrue();
        harness.assertInGraveyard(player1, "Runed Arch");
    }

    @Test
    @DisplayName("X=0 can be activated with no targets")
    void allowsZeroTargets() {
        harness.addToBattlefield(player1, new RunedArch());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runed Arch");
    }

    @Test
    @DisplayName("X=2 requires exactly two legal targets")
    void requiresExactlyXTargets() {
        harness.addToBattlefield(player1, new RunedArch());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Aurochs());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("More targets than the paid X are rejected")
    void rejectsMoreTargetsThanX() {
        harness.addToBattlefield(player1, new RunedArch());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Aurochs());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Aurochs());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with power 3 is an illegal target")
    void rejectsCreatureWithPowerThree() {
        harness.addToBattlefield(player1, new RunedArch());
        Permanent barbarians = harness.addToBattlefieldAndReturn(player1, new BalduvianBarbarians());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(barbarians.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new RunedArch());
        Permanent aurochs = harness.addToBattlefieldAndReturn(player1, new Aurochs());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 1, List.of(aurochs.getId()));
        harness.passBothPriorities();
        assertThat(aurochs.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aurochs.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("A target that becomes too powerful before resolution is ignored while legal targets are affected")
    void ignoresTargetThatBecomesTooPowerfulBeforeResolution() {
        harness.addToBattlefield(player1, new RunedArch());
        Permanent targetThatGetsTooPowerful = harness.addToBattlefieldAndReturn(player1, new Aurochs());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player1, new KjeldoranWarrior());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2,
                List.of(targetThatGetsTooPowerful.getId(), legalTarget.getId()));

        harness.castInstant(player2, 0, targetThatGetsTooPowerful.getId());
        resolveAllTriggers();

        assertThat(targetThatGetsTooPowerful.isCantBeBlocked()).isFalse();
        assertThat(legalTarget.isCantBeBlocked()).isTrue();
    }

    @Test
    void sacrificeIsPaidBeforeResolution() {
        harness.addToBattlefield(player1, new RunedArch());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Aurochs());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 1, List.of(target.getId()));

        harness.assertInGraveyard(player1, "Runed Arch");
        assertThat(countPermanents(player1, "Runed Arch")).isZero();
        assertThat(target.isCantBeBlocked()).isFalse();

        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void rejectsTappedArch() {
        Permanent arch = harness.addToBattlefieldAndReturn(player1, new RunedArch());
        arch.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Aurochs());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Runed Arch")).isEqualTo(1);
    }

    @Test
    void rejectsRepeatedTarget() {
        harness.addToBattlefield(player1, new RunedArch());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Aurochs());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNoncreatureTarget() {
        Permanent arch = harness.addToBattlefieldAndReturn(player1, new RunedArch());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(arch.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotResolveWhenEveryTargetBecomesIllegal() {
        harness.addToBattlefield(player1, new RunedArch());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Aurochs());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 1, List.of(target.getId()));
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.isCantBeBlocked()).isFalse();
        harness.assertInGraveyard(player1, "Runed Arch");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void requiresPaymentOfX() {
        harness.addToBattlefield(player1, new RunedArch());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Aurochs());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Runed Arch")).isEqualTo(1);
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void increasingPowerAfterResolutionDoesNotRemoveUnblockability() {
        harness.addToBattlefield(player1, new RunedArch());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Aurochs());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }
}
