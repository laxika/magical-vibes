package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CloudcrownOak;
import com.github.laxika.magicalvibes.cards.t.ThundercloudShaman;
import com.github.laxika.magicalvibes.cards.t.TimberProtector;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoldmeadowDodger.class, ThundercloudShaman.class, CloudcrownOak.class, TimberProtector.class})
class GoldmeadowDodgerTest extends BaseCardTest {

    @Test
    @DisplayName("Goldmeadow Dodger cannot be blocked by a creature with power 4 or greater")
    void cannotBeBlockedByPower4OrGreater() {
        addDodger();
        addCreatureReady(player2, new ThundercloudShaman()); // 4/4

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Goldmeadow Dodger can be blocked by a creature with power 3 or less")
    void canBeBlockedByPower3OrLess() {
        addDodger();
        addCreatureReady(player2, new CloudcrownOak()); // 3/4

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("Blocker power after continuous effects determines legality")
    void usesEffectivePowerAfterContinuousEffects() {
        addDodger();
        addCreatureReady(player2, new TimberProtector());
        addCreatureReady(player2, new CloudcrownOak()); // 3/4, becomes 4/5

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    private void addDodger() {
        addCreatureReady(player1, new GoldmeadowDodger());
    }
}
