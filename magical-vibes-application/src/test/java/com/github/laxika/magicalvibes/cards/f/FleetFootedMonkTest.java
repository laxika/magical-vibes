package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MuckRats;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleetFootedMonk.class, GrizzlyBears.class, MuckRats.class})
class FleetFootedMonkTest extends BaseCardTest {

    @Test
    @DisplayName("Fleet-Footed Monk cannot be blocked by a creature with power 2 or greater")
    void cannotBeBlockedByPower2OrGreater() {
        attackingMonk();

        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Fleet-Footed Monk can be blocked by a creature with power 1 or less")
    void canBeBlockedByPower1OrLess() {
        attackingMonk();

        addCreatureReady(player2, new MuckRats());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    private Permanent attackingMonk() {
        Permanent monk = addCreatureReady(player1, new FleetFootedMonk());
        monk.setAttacking(true);
        return monk;
    }
}
