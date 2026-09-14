package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnortingGahr.class, FreshVolunteers.class})
class SnortingGahrTest extends BaseCardTest {

    @Test
    @DisplayName("When Snorting Gahr becomes blocked, it gets +2/+2 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent gahr = addReadyGahr(player1);
        gahr.setAttacking(true);
        addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gahr.getPowerModifier()).isEqualTo(2);
        assertThat(gahr.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple blockers still cause only one +2/+2 trigger")
    void becomesBlockedByMultipleCreaturesGetsOneBoost() {
        Permanent gahr = addReadyGahr(player1);
        gahr.setAttacking(true);
        addCreatureReady(player2, new FreshVolunteers());
        addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(gahr.getPowerModifier()).isEqualTo(2);
        assertThat(gahr.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Snorting Gahr gets no boost when it is unblocked")
    void unblockedGetsNoBoost() {
        Permanent gahr = addReadyGahr(player1);
        gahr.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gahr.getPowerModifier()).isZero();
        assertThat(gahr.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent gahr = addReadyGahr(player1);
        gahr.setAttacking(true);
        addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gahr.getPowerModifier()).isZero();
        assertThat(gahr.getToughnessModifier()).isZero();
    }

    private Permanent addReadyGahr(Player player) {
        return addCreatureReady(player, new SnortingGahr());
    }
}
