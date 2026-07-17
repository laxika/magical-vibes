package com.github.laxika.magicalvibes.cards.c;

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

class ChubToadTest extends BaseCardTest {

    @Test
    @DisplayName("When Chub Toad becomes blocked, it gets +2/+2 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent toad = addReadyToad(player1);
        toad.setAttacking(true);
        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(toad.getPowerModifier()).isEqualTo(2);
        assertThat(toad.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Chub Toad blocks, it gets +2/+2 until end of turn")
    void blocksGetsBoost() {
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);
        Permanent toad = addReadyToad(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(toad.getPowerModifier()).isEqualTo(2);
        assertThat(toad.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Chub Toad is unblocked, it gets no boost")
    void unblockedNoBoost() {
        Permanent toad = addReadyToad(player1);
        toad.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(toad.getPowerModifier()).isZero();
        assertThat(toad.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent toad = addReadyToad(player1);
        toad.setAttacking(true);
        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(toad.getPowerModifier()).isZero();
        assertThat(toad.getToughnessModifier()).isZero();
    }

    private Permanent addReadyToad(Player player) {
        Permanent permanent = new Permanent(new ChubToad());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyBears(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
