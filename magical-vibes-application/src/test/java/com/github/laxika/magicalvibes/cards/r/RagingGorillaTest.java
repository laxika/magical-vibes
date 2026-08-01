package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagingGorillaTest extends BaseCardTest {

    @Test
    @DisplayName("When Raging Gorilla becomes blocked, it gets +2/-2 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent gorilla = addReadyGorilla(player1);
        gorilla.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gorilla.getPowerModifier()).isEqualTo(2);
        assertThat(gorilla.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("When Raging Gorilla blocks, it gets +2/-2 until end of turn")
    void blocksGetsBoost() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent gorilla = addReadyGorilla(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gorilla.getPowerModifier()).isEqualTo(2);
        assertThat(gorilla.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("When Raging Gorilla is unblocked, it gets no boost")
    void unblockedNoBoost() {
        Permanent gorilla = addReadyGorilla(player1);
        gorilla.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gorilla.getPowerModifier()).isZero();
        assertThat(gorilla.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent gorilla = addReadyGorilla(player1);
        gorilla.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gorilla.getPowerModifier()).isZero();
        assertThat(gorilla.getToughnessModifier()).isZero();
    }

    private Permanent addReadyGorilla(Player player) {
        Permanent permanent = new Permanent(new RagingGorilla());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
