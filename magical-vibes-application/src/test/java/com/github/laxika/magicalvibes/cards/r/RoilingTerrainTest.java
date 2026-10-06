package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.ArborElf;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TerraEternal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoilingTerrain.class, Forest.class, Island.class, ArborElf.class, TerraEternal.class})
class RoilingTerrainTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land and counts land cards in its controller's graveyard")
    void destroysLandAndDealsDamageBasedOnControllerGraveyard() {
        harness.addToBattlefield(player2, new Forest());
        harness.setGraveyard(player2, List.of(
                new Island(),
                new Island(),
                new ArborElf()));

        UUID targetId = harness.getPermanentId(player2, "Forest");
        castRoilingTerrain(targetId);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new ArborElf());
        harness.setHand(player1, List.of(new RoilingTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Arbor Elf");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Can destroy your own land and deals damage to you")
    void damagesControllerOfOwnLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setGraveyard(player1, List.of(new Island()));
        harness.setGraveyard(player2, List.of(new Island(), new Island(), new Island()));

        castRoilingTerrain(harness.getPermanentId(player1, "Forest"));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Indestructible land survives but its controller still takes damage")
    void dealsDamageEvenWhenLandCannotBeDestroyed() {
        harness.addToBattlefield(player1, new TerraEternal());
        harness.addToBattlefield(player2, new Forest());
        harness.setGraveyard(player2, List.of(new Island(), new Island(), new ArborElf()));

        castRoilingTerrain(harness.getPermanentId(player2, "Forest"));

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An indestructible land with no land cards in its controller's graveyard deals no damage")
    void emptyGraveyardDealsZeroDamageWhenLandSurvives() {
        harness.addToBattlefield(player1, new TerraEternal());
        harness.addToBattlefield(player2, new Forest());
        harness.setGraveyard(player2, List.of(new ArborElf()));

        castRoilingTerrain(harness.getPermanentId(player2, "Forest"));

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals no damage if the only target leaves before resolution")
    void doesNotDealDamageWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new Forest());
        harness.setGraveyard(player2, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new RoilingTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Forest"));

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Roiling Terrain");
    }

    private void castRoilingTerrain(UUID targetId) {
        harness.setHand(player1, List.of(new RoilingTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
