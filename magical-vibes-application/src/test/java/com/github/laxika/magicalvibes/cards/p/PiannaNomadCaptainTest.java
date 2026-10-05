package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiannaNomadCaptain.class, AvenFlock.class})
class PiannaNomadCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Pianna gives attacking creatures +1/+1")
    void boostsAllAttackers() {
        Permanent pianna = addCreatureReady(player1, new PiannaNomadCaptain());
        Permanent flock = addCreatureReady(player1, new AvenFlock());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(pianna.getPowerModifier()).isEqualTo(1);
        assertThat(pianna.getToughnessModifier()).isEqualTo(1);
        assertThat(flock.getPowerModifier()).isEqualTo(1);
        assertThat(flock.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Pianna does not boost creatures that are not attacking")
    void doesNotBoostNonAttackers() {
        addCreatureReady(player1, new PiannaNomadCaptain());
        Permanent flock = addCreatureReady(player1, new AvenFlock());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(flock.getPowerModifier()).isZero();
        assertThat(flock.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Pianna boosts a creature put onto the battlefield attacking before resolution")
    void boostsCreatureEnteringAttackingBeforeResolution() {
        addCreatureReady(player1, new PiannaNomadCaptain());

        declareAttackers(List.of(0));
        // Model a creature entering attacking while Pianna's trigger is on the stack.
        Permanent flock = addCreatureReady(player1, new AvenFlock());
        flock.setAttackTarget(player2.getId());
        flock.setAttacking(true);
        resolveAllTriggers();

        assertThat(flock.getPowerModifier()).isEqualTo(1);
        assertThat(flock.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Pianna does not trigger when only another creature attacks")
    void doesNotTriggerWhenPiannaStaysBack() {
        Permanent pianna = addCreatureReady(player1, new PiannaNomadCaptain());
        Permanent flock = addCreatureReady(player1, new AvenFlock());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(pianna.getPowerModifier()).isZero();
        assertThat(pianna.getToughnessModifier()).isZero();
        assertThat(flock.getPowerModifier()).isZero();
        assertThat(flock.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Pianna checks attacking status when its trigger resolves")
    void doesNotBoostCreatureRemovedFromCombatBeforeResolution() {
        Permanent pianna = addCreatureReady(player1, new PiannaNomadCaptain());
        Permanent flock = addCreatureReady(player1, new AvenFlock());

        declareAttackers(List.of(0, 1));
        // Model removal from combat in response to the trigger.
        flock.setAttacking(false);
        flock.setAttackTarget(null);
        resolveAllTriggers();

        assertThat(pianna.getPowerModifier()).isEqualTo(1);
        assertThat(pianna.getToughnessModifier()).isEqualTo(1);
        assertThat(flock.getPowerModifier()).isZero();
        assertThat(flock.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Pianna's resolved boost remains after a creature stops attacking")
    void boostRemainsAfterRemovalFromCombat() {
        addCreatureReady(player1, new PiannaNomadCaptain());
        Permanent flock = addCreatureReady(player1, new AvenFlock());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        flock.setAttacking(false);
        flock.setAttackTarget(null);

        assertThat(flock.getPowerModifier()).isEqualTo(1);
        assertThat(flock.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Pianna's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new PiannaNomadCaptain());
        Permanent flock = addCreatureReady(player1, new AvenFlock());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(flock.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(flock.getPowerModifier()).isZero();
        assertThat(flock.getToughnessModifier()).isZero();
    }
}
