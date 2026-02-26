package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetLandAndDamageControllerEffect;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeltTerrainTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct card properties")
    void hasCorrectProperties() {
        MeltTerrain card = new MeltTerrain();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(DestroyTargetLandAndDamageControllerEffect.class);

        DestroyTargetLandAndDamageControllerEffect effect =
                (DestroyTargetLandAndDamageControllerEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.damage()).isEqualTo(2);
    }

    // ===== Casting =====

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
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetId);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving destroys target land and deals 2 damage to its controller")
    void resolvingDestroysLandAndDealsDamage() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new MeltTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can target any land type (Island)")
    void canTargetIsland() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new MeltTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can target own land (damage dealt to self)")
    void canTargetOwnLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new MeltTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player1, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Target validation =====

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MeltTerrain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    // ===== Fizzle =====

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
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Melt Terrain"));
    }
}
