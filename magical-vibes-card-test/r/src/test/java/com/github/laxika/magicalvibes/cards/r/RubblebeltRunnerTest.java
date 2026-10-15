package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RubblebeltRunner.class, GrizzlyBears.class})
class RubblebeltRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Rubblebelt Runner can't be blocked by creature tokens")
    void cannotBeBlockedByCreatureToken() {
        attackingRunner();

        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        addCreatureReady(player2, tokenCard);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rubblebelt Runner can be blocked by nontoken creatures")
    void canBeBlockedByNontokenCreature() {
        attackingRunner();

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent attackingRunner() {
        Permanent runner = addCreatureReady(player1, new RubblebeltRunner());
        runner.setAttacking(true);
        return runner;
    }

    @Test
    @DisplayName("A token copy of Rubblebelt Runner retains its blocking restriction")
    void tokenRunnerCannotBeBlockedByCreatureToken() {
        Card runnerCard = new RubblebeltRunner();
        runnerCard.setToken(true);
        Permanent attacker = addCreatureReady(player1, runnerCard);
        attacker.setAttacking(true);

        Card blockerCard = new GrizzlyBears();
        blockerCard.setToken(true);
        addCreatureReady(player2, blockerCard);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rubblebelt Runner does not prevent tokens from blocking other attackers")
    void creatureTokenCanBlockAnotherAttacker() {
        attackingRunner();
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        otherAttacker.setAttacking(true);

        Card blockerCard = new GrizzlyBears();
        blockerCard.setToken(true);
        Permanent blocker = addCreatureReady(player2, blockerCard);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargets()).containsExactly(1);
    }
}
