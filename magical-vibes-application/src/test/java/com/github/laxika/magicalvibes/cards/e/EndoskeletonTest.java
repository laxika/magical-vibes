package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.Guma;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Endoskeleton.class, Guma.class, WornPowerstone.class})
class EndoskeletonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives the target creature +0/+3")
    void resolvingGrantsBoost() {
        addReadyEndoskeleton(player1);
        Permanent guma = addReadyGuma(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, guma.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guma)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guma)).isEqualTo(5);
    }

    @Test
    @DisplayName("Activating the ability taps Endoskeleton")
    void activatingTapsEndoskeleton() {
        Permanent endoskeleton = addReadyEndoskeleton(player1);
        Permanent guma = addReadyGuma(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, guma.getId());

        assertThat(endoskeleton.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost persists past end of turn while Endoskeleton stays tapped")
    void boostSurvivesEndOfTurnWhileTapped() {
        addReadyEndoskeleton(player1);
        Permanent guma = addReadyGuma(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, guma.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guma)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guma)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost ends when Endoskeleton becomes untapped")
    void boostEndsWhenEndoskeletonUntaps() {
        Permanent endoskeleton = addReadyEndoskeleton(player1);
        Permanent guma = addReadyGuma(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, guma.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, guma)).isEqualTo(5);

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(endoskeleton.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, guma)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guma)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost persists when the controller keeps Endoskeleton tapped")
    void boostPersistsWhenKeptTapped() {
        Permanent endoskeleton = addReadyEndoskeleton(player1);
        Permanent guma = addReadyGuma(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, guma.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(endoskeleton.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, guma)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guma)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyEndoskeleton(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new WornPowerstone());
        artifact.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        addReadyEndoskeleton(player1);
        Permanent guma = addReadyGuma(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, guma.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guma)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guma)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot activate the ability while Endoskeleton is already tapped")
    void cannotActivateWhileTapped() {
        Permanent endoskeleton = addReadyEndoskeleton(player1);
        Permanent guma = addReadyGuma(player1);
        endoskeleton.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, guma.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost does not resume when Endoskeleton is tapped again after untapping")
    void boostDoesNotResumeAfterSourceIsRetapped() {
        Permanent endoskeleton = addReadyEndoskeleton(player1);
        Permanent firstGuma = addReadyGuma(player1);
        Permanent secondGuma = addReadyGuma(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, firstGuma.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, firstGuma)).isEqualTo(5);

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(endoskeleton.isTapped()).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, firstGuma)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, secondGuma.getId());
        harness.passBothPriorities();

        assertThat(endoskeleton.isTapped()).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, firstGuma)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondGuma)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost ends when Endoskeleton leaves the battlefield")
    void boostEndsWhenEndoskeletonLeavesBattlefield() {
        Permanent endoskeleton = addReadyEndoskeleton(player1);
        Permanent guma = addReadyGuma(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, guma.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, guma)).isEqualTo(5);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, endoskeleton));

        assertThat(gqs.getEffectiveToughness(gd, guma)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost is not created if Endoskeleton leaves before the ability resolves")
    void boostDoesNotApplyIfEndoskeletonLeavesBeforeResolution() {
        Permanent endoskeleton = addReadyEndoskeleton(player1);
        Permanent guma = addReadyGuma(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, guma.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, endoskeleton));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guma)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guma)).isEqualTo(2);
    }

    private Permanent addReadyEndoskeleton(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Endoskeleton());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyGuma(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Guma());
        perm.setSummoningSick(false);
        return perm;
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
