package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({SteelLeafChampion.class, GrizzlyBears.class, HillGiant.class})
class SteelLeafChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Steel Leaf Champion cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedByPower2OrLess() {
        attackingChampion();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Steel Leaf Champion can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPower3OrGreater() {
        attackingChampion();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        giant.setSummoningSick(false);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("A creature raised from power 2 to power 3 can block Steel Leaf Champion")
    void increasedPowerAllowsBlocking() {
        attackingChampion();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setPowerModifier(1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("A creature reduced from power 3 to power 2 cannot block Steel Leaf Champion")
    void decreasedPowerPreventsBlocking() {
        attackingChampion();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        giant.setPowerModifier(-1);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    private Permanent attackingChampion() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new SteelLeafChampion());
        champion.setSummoningSick(false);
        champion.setAttacking(true);
        return champion;
    }
}
