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
    @DisplayName("Pianna boosts an opponent-controlled attacking creature")
    void boostsOpponentControlledAttacker() {
        addCreatureReady(player1, new PiannaNomadCaptain());
        Permanent opponentFlock = addCreatureReady(player2, new AvenFlock());

        declareAttackers(List.of(0));
        // Model an effect putting an opponent-controlled creature onto the battlefield attacking.
        opponentFlock.setAttackTarget(player1.getId());
        opponentFlock.setAttacking(true);
        resolveAllTriggers();

        assertThat(opponentFlock.getPowerModifier()).isEqualTo(1);
        assertThat(opponentFlock.getToughnessModifier()).isEqualTo(1);
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
