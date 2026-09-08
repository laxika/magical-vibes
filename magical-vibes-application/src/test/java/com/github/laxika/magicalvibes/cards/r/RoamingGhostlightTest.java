package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoamingGhostlight.class, GrizzlyBears.class, WindSpirit.class})
class RoamingGhostlightTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to one target non-Spirit creature")
    void etbReturnsNonSpiritCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castGhostlight(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Roaming Ghostlight");
    }

    @Test
    @DisplayName("A Spirit creature is not a legal target")
    void cannotTargetSpiritCreature() {
        harness.addToBattlefield(player2, new WindSpirit());
        UUID spiritId = harness.getPermanentId(player2, "Wind Spirit");
        harness.setHand(player1, List.of(new RoamingGhostlight()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, spiritId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a non-Spirit creature");
    }

    @Test
    @DisplayName("Can enter the battlefield without a target")
    void canEnterWithoutTarget() {
        harness.setHand(player1, List.of(new RoamingGhostlight()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Roaming Ghostlight");
    }

    private void castGhostlight(UUID targetId) {
        harness.setHand(player1, List.of(new RoamingGhostlight()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
