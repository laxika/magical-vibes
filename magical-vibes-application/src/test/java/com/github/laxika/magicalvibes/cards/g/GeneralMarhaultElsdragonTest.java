package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeneralMarhaultElsdragon.class, GrizzlyBears.class})
class GeneralMarhaultElsdragonTest extends BaseCardTest {

    @Test
    @DisplayName("A blocked creature you control gets +3/+3 for each blocker")
    void blockedAllyGetsThreePerBlocker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player1, new GeneralMarhaultElsdragon());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(6);
        assertThat(attacker.getToughnessModifier()).isEqualTo(6);
    }

    @Test
    @DisplayName("No trigger is created when a creature you control is unblocked")
    void unblockedCreatureGetsNoBoost() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player1, new GeneralMarhaultElsdragon());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player1, new GeneralMarhaultElsdragon());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(3);
        assertThat(attacker.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }
}
