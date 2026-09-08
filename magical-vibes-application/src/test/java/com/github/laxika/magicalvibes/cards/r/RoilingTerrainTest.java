package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoilingTerrainTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land and counts land cards in its controller's graveyard")
    void destroysLandAndDealsDamageBasedOnControllerGraveyard() {
        harness.addToBattlefield(player2, new Forest());
        gd.playerGraveyards.get(player2.getId()).addAll(List.of(
                new Island(),
                new Island(),
                new GrizzlyBears()));

        UUID targetId = harness.getPermanentId(player2, "Forest");
        castRoilingTerrain(targetId);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RoilingTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private void castRoilingTerrain(UUID targetId) {
        harness.setHand(player1, List.of(new RoilingTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
