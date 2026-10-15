package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
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

@CardUsed({BloodFrenzy.class, HornedTurtle.class})
class BloodFrenzyTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creature gets +4/+0 and is destroyed at the next end step")
    void boostsAndDestroysAttacker() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new BloodFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        Permanent boosted = gqs.findPermanentById(gd, attacker.getId());
        assertThat(gqs.getEffectivePower(gd, boosted)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, boosted)).isEqualTo(4);

        drainEndStep();

        harness.assertNotOnBattlefield(player1, "Horned Turtle");
        harness.assertInGraveyard(player1, "Horned Turtle");
    }

    @Test
    @DisplayName("A blocking creature is a legal target")
    void blockingCreatureIsLegalTarget() {
        harness.forceActivePlayer(player1);
        Permanent blocker = addCreatureReady(player2, new HornedTurtle());
        blocker.setBlocking(true);
        harness.setHand(player1, List.of(new BloodFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(gqs.getEffectivePower(gd, gqs.findPermanentById(gd, blocker.getId()))).isEqualTo(5);

        drainEndStep();

        harness.assertInGraveyard(player2, "Horned Turtle");
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetIdleCreature() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());
        attacker.setAttacking(true);
        Permanent idle = addCreatureReady(player2, new HornedTurtle());
        harness.setHand(player1, List.of(new BloodFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, idle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking or blocking creature");
    }

    @Test
    @DisplayName("Cannot cast once the combat damage step has been reached")
    void cannotCastAtCombatDamage() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new BloodFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast after combat")
    void cannotCastAfterCombat() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new BloodFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Removing the target from combat before resolution prevents both effects")
    void targetLeavesCombatBeforeResolution() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new BloodFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castInstant(player1, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Blood Frenzy");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Horned Turtle");
    }

    @Test
    @DisplayName("Destruction still happens when the creature stops attacking after resolution")
    void delayedDestructionDoesNotRequireCreatureToRemainAttacking() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new BloodFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0, attacker.getId());
        attacker.setAttacking(false);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        drainEndStep();

        harness.assertNotOnBattlefield(player1, "Horned Turtle");
        harness.assertInGraveyard(player1, "Horned Turtle");
    }

    private void drainEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }
}
