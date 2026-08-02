package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasakoTheHumorlessTest extends BaseCardTest {

    @Test
    @DisplayName("Masako lets a tapped creature you control block")
    void tappedCreatureCanBlockWithMasako() {
        harness.addToBattlefield(player2, new MasakoTheHumorless());
        Permanent blocker = addCreature(player2);
        blocker.tap();
        Permanent attacker = addAttacker(player1);

        beginBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped creature cannot block without Masako")
    void tappedCreatureCannotBlockWithoutMasako() {
        Permanent blocker = addCreature(player2);
        blocker.tap();
        Permanent attacker = addAttacker(player1);

        beginBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Masako does not let an opponent's tapped creature block")
    void tappedOpponentCreatureCannotBlockWithMasako() {
        harness.addToBattlefield(player1, new MasakoTheHumorless());
        Permanent blocker = addCreature(player2);
        blocker.tap();
        Permanent attacker = addAttacker(player1);

        beginBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = addCreature(player);
        creature.setAttacking(true);
        return creature;
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
