package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrossProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Dross Prowler cannot be blocked by a nonblack, nonartifact creature")
    void cannotBeBlockedByNonblackNonartifactCreature() {
        Permanent prowler = attackingProwler();
        gd.playerBattlefields.get(player1.getId()).add(prowler);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Dross Prowler (fear)");
    }

    @Test
    @DisplayName("Dross Prowler can be blocked by an artifact creature")
    void canBeBlockedByArtifactCreature() {
        Permanent prowler = attackingProwler();
        gd.playerBattlefields.get(player1.getId()).add(prowler);

        Permanent ornithopter = new Permanent(new Ornithopter());
        ornithopter.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ornithopter);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    private Permanent attackingProwler() {
        Permanent prowler = new Permanent(new DrossProwler());
        prowler.setSummoningSick(false);
        prowler.setAttacking(true);
        return prowler;
    }
}
