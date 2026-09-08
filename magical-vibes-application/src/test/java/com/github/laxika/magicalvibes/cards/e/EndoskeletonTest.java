package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EndoskeletonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives the target creature +0/+3")
    void resolvingGrantsBoost() {
        addReadyEndoskeleton(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
    }

    @Test
    @DisplayName("Activating the ability taps Endoskeleton")
    void activatingTapsEndoskeleton() {
        Permanent endoskeleton = addReadyEndoskeleton(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());

        assertThat(endoskeleton.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost persists past end of turn while Endoskeleton stays tapped")
    void boostSurvivesEndOfTurnWhileTapped() {
        addReadyEndoskeleton(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost ends when Endoskeleton becomes untapped")
    void boostEndsWhenEndoskeletonUntaps() {
        Permanent endoskeleton = addReadyEndoskeleton(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(endoskeleton.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost persists when the controller keeps Endoskeleton tapped")
    void boostPersistsWhenKeptTapped() {
        Permanent endoskeleton = addReadyEndoskeleton(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(endoskeleton.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyEndoskeleton(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        artifact.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyEndoskeleton(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Endoskeleton());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyBear(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        perm.setSummoningSick(false);
        return perm;
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
