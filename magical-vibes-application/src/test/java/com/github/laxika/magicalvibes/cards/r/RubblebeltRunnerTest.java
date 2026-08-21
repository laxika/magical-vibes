package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RubblebeltRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Rubblebelt Runner can't be blocked by creature tokens")
    void cannotBeBlockedByCreatureToken() {
        Permanent attacker = attackingRunner();
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = new Permanent(tokenCard);
        token.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(token);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rubblebelt Runner can be blocked by nontoken creatures")
    void canBeBlockedByNontokenCreature() {
        Permanent attacker = attackingRunner();
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent attackingRunner() {
        Permanent runner = new Permanent(new RubblebeltRunner());
        runner.setSummoningSick(false);
        runner.setAttacking(true);
        return runner;
    }
}
