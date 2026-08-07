package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeathcultRogueTest extends BaseCardTest {

    @Test
    @DisplayName("Deathcult Rogue cannot be blocked by a non-Rogue creature")
    void cannotBeBlockedByNonRogue() {
        gd.playerBattlefields.get(player1.getId()).add(attackingRogue());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Rogues");
    }

    @Test
    @DisplayName("Deathcult Rogue can be blocked by a Rogue")
    void canBeBlockedByRogue() {
        gd.playerBattlefields.get(player1.getId()).add(attackingRogue());

        Permanent blocker = new Permanent(new DeathcultRogue());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    private Permanent attackingRogue() {
        Permanent rogue = new Permanent(new DeathcultRogue());
        rogue.setSummoningSick(false);
        rogue.setAttacking(true);
        return rogue;
    }
}
