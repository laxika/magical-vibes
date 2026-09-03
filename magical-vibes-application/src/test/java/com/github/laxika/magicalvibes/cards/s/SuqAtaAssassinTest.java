package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Breezekeeper;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.cards.u.UrborgMindsucker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuqAtaAssassin.class, Breezekeeper.class, PhyrexianWalker.class, UrborgMindsucker.class})
class SuqAtaAssassinTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new SuqAtaAssassin());
        attacker.setAttacking(true);
        return attacker;
    }

    @Test
    @DisplayName("Unblocked attacker gives the defending player a poison counter")
    void unblockedGivesPoison() {
        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        // Advance into the declare-blockers step (the defender has no blockers), which fires the
        // "attacks and isn't blocked" trigger onto the stack, then resolve it.
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Blocked attacker gives no poison counter")
    void blockedNoPoison() {
        // Phyrexian Walker is an artifact creature, so it can block through Fear.
        Permanent blocker = addCreatureReady(player2, new PhyrexianWalker());

        addAttacker();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking")
    void nonblackNonartifactCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new Breezekeeper());
        addAttacker();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Fear allows a black creature to block")
    void blackCreatureCanBlock() {
        Permanent blocker = addCreatureReady(player2, new UrborgMindsucker());
        addAttacker();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
