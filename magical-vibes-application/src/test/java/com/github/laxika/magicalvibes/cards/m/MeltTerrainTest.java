package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeltTerrain.class, Forest.class, Island.class, GrizzlyBears.class, DarksteelCitadel.class})
class MeltTerrainTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new MeltTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Melt Terrain");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target land and deals 2 damage to its controller")
    void resolvingDestroysLandAndDealsDamage() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new MeltTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can target any land type (Island)")
    void canTargetIsland() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new MeltTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can target own land (damage dealt to self)")
    void canTargetOwnLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new MeltTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player1, "Forest");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new Forest()); // valid target so spell is playable
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MeltTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Fizzles if target land is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new MeltTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Melt Terrain goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new MeltTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Melt Terrain");
    }

    @Test
    @DisplayName("An indestructible land survives but its controller still takes damage")
    void indestructibleLandStillCausesDamage() {
        harness.addToBattlefield(player2, new DarksteelCitadel());
        MeltTerrain spell = new MeltTerrain();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Darksteel Citadel");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertNotInGraveyard(player2, "Darksteel Citadel");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(harness.getGameData().damageDealtThisTurnBySource.get(spell.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Melt Terrain");
    }
}
