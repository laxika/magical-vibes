package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
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

class LongFinnedSkywhaleTest extends BaseCardTest {

    @Test
    @DisplayName("Long-Finned Skywhale cannot be blocked by a nonflying creature")
    void cannotBeBlockedByNonflyingCreature() {
        gd.playerBattlefields.get(player1.getId()).add(attackingSkywhale());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Long-Finned Skywhale can be blocked by a flying creature")
    void canBeBlockedByFlyingCreature() {
        gd.playerBattlefields.get(player1.getId()).add(attackingSkywhale());

        Permanent flyer = new Permanent(new AvenFisher());
        flyer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(flyer);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    private Permanent attackingSkywhale() {
        Permanent skywhale = new Permanent(new LongFinnedSkywhale());
        skywhale.setSummoningSick(false);
        skywhale.setAttacking(true);
        return skywhale;
    }
}
