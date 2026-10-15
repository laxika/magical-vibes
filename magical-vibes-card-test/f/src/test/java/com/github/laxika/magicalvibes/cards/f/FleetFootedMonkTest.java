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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1})
    @DisplayName("Reduced-power creatures can block using their current power")
    void reducedPowerCreatureCanBlock(int powerModifier) {
        attackingMonk();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setPowerModifier(powerModifier);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("A creature increased to power 2 or greater cannot block")
    void increasedPowerCreatureCannotBlock(int powerModifier) {
        attackingMonk();
        Permanent blocker = addCreatureReady(player2, new MuckRats());
        blocker.setPowerModifier(powerModifier);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    private void attackingMonk() {
        Permanent monk = addCreatureReady(player1, new FleetFootedMonk());
        monk.setAttacking(true);
    }
}
