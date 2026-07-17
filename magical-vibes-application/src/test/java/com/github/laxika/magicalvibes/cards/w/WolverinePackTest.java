package com.github.laxika.magicalvibes.cards.w;

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

class WolverinePackTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rampage 2 grants no bonus")
    void oneBlockerGivesNothing() {
        Permanent pack = addReadyPack(player1);
        pack.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(pack.getPowerModifier()).isZero();
        assertThat(pack.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers Rampage 2 grants +2/+2 until end of turn")
    void twoBlockersGivesPlusTwo() {
        Permanent pack = addReadyPack(player1);
        pack.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(pack.getPowerModifier()).isEqualTo(2);
        assertThat(pack.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("With three blockers Rampage 2 grants +4/+4 until end of turn")
    void threeBlockersGivesPlusFour() {
        Permanent pack = addReadyPack(player1);
        pack.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(pack.getPowerModifier()).isEqualTo(4);
        assertThat(pack.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent pack = addReadyPack(player1);
        pack.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(pack.getPowerModifier()).isZero();
        assertThat(pack.getToughnessModifier()).isZero();
    }

    private Permanent addReadyPack(Player player) {
        Permanent permanent = new Permanent(new WolverinePack());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addReadyBears(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
