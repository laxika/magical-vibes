package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBaloth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenalishCavalry.class, DurkwoodBaloth.class})
class BenalishCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void blockerWithoutFlankingGetsMinusOneMinusOne() {
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());
        cavalry.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DurkwoodBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(4);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("A blocker that also has flanking is unaffected")
    void blockerWithFlankingIsUnaffected() {
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());
        cavalry.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BenalishCavalry());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flanking weakens each non-flanking blocker")
    void eachNonFlankingBlockerGetsMinusOneMinusOne() {
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());
        cavalry.setAttacking(true);
        Permanent blocker1 = addCreatureReady(player2, new DurkwoodBaloth());
        Permanent blocker2 = addCreatureReady(player2, new DurkwoodBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(blocker1.getEffectivePower()).isEqualTo(4);
        assertThat(blocker1.getEffectiveToughness()).isEqualTo(4);
        assertThat(blocker2.getEffectivePower()).isEqualTo(4);
        assertThat(blocker2.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Flanking's penalty wears off at end of turn")
    void flankingPenaltyWearsOffAtEndOfTurn() {
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());
        cavalry.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DurkwoodBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getEffectivePower()).isEqualTo(4);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(5);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("An unblocked creature with flanking creates no trigger")
    void unblockedCreatesNoTrigger() {
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());
        cavalry.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }
}
