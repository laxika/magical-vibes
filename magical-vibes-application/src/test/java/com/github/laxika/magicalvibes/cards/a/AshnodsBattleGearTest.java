package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({AshnodsBattleGear.class, GrizzlyBears.class, HillGiant.class})
class AshnodsBattleGearTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives the target creature you control +2/-2")
    void resolvingGrantsBoost() {
        addReadyGear(player1);
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the ability taps Ashnod's Battle Gear")
    void activatingTapsGear() {
        Permanent gear = addReadyGear(player1);
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, giant.getId());

        assertThat(gear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The -2 toughness can be lethal via state-based actions")
    void negativeToughnessCanBeLethal() {
        addReadyGear(player1);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // 2/2 becomes 4/0 -> dies as a state-based action.
        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("Boost persists past end of turn while the artifact stays tapped")
    void boostSurvivesEndOfTurnWhileTapped() {
        addReadyGear(player1);
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost ends when the artifact becomes untapped")
    void boostEndsWhenArtifactUntaps() {
        Permanent gear = addReadyGear(player1);
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);

        // Advance to player1's untap step and choose to untap the gear.
        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(gear.isTapped()).isFalse();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost persists when the controller keeps the artifact tapped")
    void boostPersistsWhenKeptTapped() {
        Permanent gear = addReadyGear(player1);
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();

        // Advance to player1's untap step and choose NOT to untap.
        advanceToNextTurnWithMayChoice(player2, false);
        assertThat(gear.isTapped()).isTrue();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost ends when the artifact leaves the battlefield")
    void boostEndsWhenArtifactRemoved() {
        Permanent gear = addReadyGear(player1);
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, gear));

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetCreatureYouDontControl() {
        addReadyGear(player1);
        Permanent enemyGiant = addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemyGiant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void cannotTargetNonCreaturePermanentYouControl() {
        addReadyGear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(this::activateNonCreatureTarget)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostDoesNotApplyIfArtifactLeavesBeforeResolution() {
        Permanent gear = addReadyGear(player1);
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, gear));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    // ===== Helpers =====

    private Permanent addReadyGear(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AshnodsBattleGear());
        perm.setSummoningSick(false);
        return perm;
    }

    private void activateNonCreatureTarget() {
        harness.activateAbility(player1, 0, null,
                gd.playerBattlefields.get(player1.getId()).getFirst().getId());
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
