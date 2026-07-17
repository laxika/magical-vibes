package com.github.laxika.magicalvibes.cards.t;

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

class TawnossWeaponryTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives the target creature +1/+1")
    void resolvingGrantsBoost() {
        addReadyWeaponry(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating the ability taps Tawnos's Weaponry")
    void activatingTapsWeaponry() {
        Permanent weaponry = addReadyWeaponry(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());

        assertThat(weaponry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost persists past end of turn while the artifact stays tapped")
    void boostSurvivesEndOfTurnWhileTapped() {
        addReadyWeaponry(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost ends when the artifact becomes untapped")
    void boostEndsWhenArtifactUntaps() {
        Permanent weaponry = addReadyWeaponry(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);

        // Advance to player1's untap step and choose to untap Tawnos's Weaponry.
        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(weaponry.isTapped()).isFalse();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost persists when the controller keeps the artifact tapped")
    void boostPersistsWhenKeptTapped() {
        Permanent weaponry = addReadyWeaponry(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        // Advance to player1's untap step and choose NOT to untap.
        advanceToNextTurnWithMayChoice(player2, false);
        assertThat(weaponry.isTapped()).isTrue();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost ends when the artifact leaves the battlefield")
    void boostEndsWhenArtifactRemoved() {
        Permanent weaponry = addReadyWeaponry(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);

        harness.getPermanentRemovalService().tryDestroyPermanent(gd, weaponry);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyWeaponry(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        artifact.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Helpers =====

    private Permanent addReadyWeaponry(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TawnossWeaponry());
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
        harness.passBothPriorities(); // END_STEP -> CLEANUP -> advanceTurn -> may ability prompt

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
