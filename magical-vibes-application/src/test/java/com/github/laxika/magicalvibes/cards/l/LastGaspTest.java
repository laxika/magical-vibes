package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SiegeWurm;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
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

@CardUsed({LastGasp.class, SiegeWurm.class, Watchwolf.class, Forest.class})
class LastGaspTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -3/-3 until end of turn")
    void givesMinusThreeMinusThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());

        castLastGasp(target);

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts a creature whose toughness is reduced to zero into the graveyard")
    void creatureWithZeroToughnessDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Watchwolf());

        castLastGasp(target);

        harness.assertNotOnBattlefield(player2, "Watchwolf");
        harness.assertInGraveyard(player2, "Watchwolf");
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        castLastGasp(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Last Gasp");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster without affecting other creatures")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SiegeWurm());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());

        castLastGasp(target);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Two Last Gasps cumulatively reduce toughness below zero")
    void cumulativeReductionsKillLargerCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());

        castLastGasp(target);
        harness.assertOnBattlefield(player2, "Siege Wurm");
        castLastGasp(target);

        harness.assertNotOnBattlefield(player2, "Siege Wurm");
        harness.assertInGraveyard(player2, "Siege Wurm");
    }

    @Test
    @DisplayName("The reduction remains during the end step and expires during cleanup")
    void reductionPersistsThroughEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        harness.forceStep(TurnStep.END_STEP);

        castLastGasp(target);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private void castLastGasp(Permanent target) {
        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
