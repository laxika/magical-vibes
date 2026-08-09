package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SquirmingMassTest extends BaseCardTest {

    private void addAttacker() {
        Permanent attacker = new Permanent(new SquirmingMass());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
    }

    private void prepareBlocker(Permanent blocker) {
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking")
    void nonblackNonartifactCreatureCannotBlock() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        prepareBlocker(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Fear allows a black creature to block")
    void blackCreatureCanBlock() {
        Permanent blocker = new Permanent(new BlackKnight());
        prepareBlocker(blocker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fear allows an artifact creature to block")
    void artifactCreatureCanBlock() {
        Permanent blocker = new Permanent(new Ornithopter());
        prepareBlocker(blocker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
