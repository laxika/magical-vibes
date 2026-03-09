package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArcTrailTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Arc Trail has correct card properties")
    void hasCorrectProperties() {
        ArcTrail card = new ArcTrail();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getMinTargets()).isEqualTo(2);
        assertThat(card.getMaxTargets()).isEqualTo(2);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DealOrderedDamageToAnyTargetsEffect.class);
        DealOrderedDamageToAnyTargetsEffect effect = (DealOrderedDamageToAnyTargetsEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.damageAmounts()).containsExactly(2, 1);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Arc Trail with 2 creature targets puts it on the stack")
    void castingWithTwoCreatureTargetsPutsOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castSorcery(player1, 0, List.of(id1, id2));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Arc Trail");
        assertThat(entry.getTargetPermanentIds()).containsExactly(id1, id2);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 1);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(bf.get(0).getId(), bf.get(1).getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast with only 1 target")
    void cannotCastWithOnlyOneTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot cast with duplicate targets")
    void cannotCastWithDuplicateTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId, bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Casting with invalid card index -1 throws IllegalArgumentException")
    void castingWithNegativeCardIndexThrowsCleanError() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, -1, List.of(id1, id2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid card index");
    }

    // ===== Damage to creatures =====

    @Test
    @DisplayName("Deals 2 damage to first target and 1 damage to second target")
    void dealsOrderedDamageToCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());   // Target 1: 2 damage (dies, 2 toughness)
        harness.addToBattlefield(player2, new GiantSpider());    // Target 2: 1 damage (survives, 4 toughness)
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID bearsId = bf.get(0).getId();
        UUID spiderId = bf.get(1).getId();

        harness.castSorcery(player1, 0, List.of(bearsId, spiderId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // GrizzlyBears took 2 damage (dies: 2 >= 2 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // GiantSpider took 1 damage (survives: 1 < 4 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("1 damage from second target slot does not kill a 2/2")
    void secondTargetOneDamageDoesNotKillTwoToughness() {
        harness.addToBattlefield(player2, new GiantSpider());    // Target 1: 2 damage (survives)
        harness.addToBattlefield(player2, new GrizzlyBears());   // Target 2: 1 damage (survives)
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID spiderId = bf.get(0).getId();
        UUID bearsId = bf.get(1).getId();

        harness.castSorcery(player1, 0, List.of(spiderId, bearsId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Both survive
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Damage to players =====

    @Test
    @DisplayName("Deals 2 damage to player target and 1 damage to creature target")
    void dealsDamageToPlayerAndCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Target 1 (2 dmg): player2, Target 2 (1 dmg): creature
        harness.castSorcery(player1, 0, List.of(player2.getId(), bearsId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // GrizzlyBears took 1 damage (survives: 1 < 2)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Deals damage to both players")
    void dealsDamageToBothPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player 2 took 2 damage (first target)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // Player 1 took 1 damage (second target)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    // ===== Partial resolution =====

    @Test
    @DisplayName("Partially resolves when first creature target is removed")
    void partiallyResolvesWhenFirstTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Target 1 (2 dmg): creature, Target 2 (1 dmg): player2
        harness.castSorcery(player1, 0, List.of(bearsId, player2.getId()));

        // Remove the creature before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player 2 still took 1 damage from second target
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== Stack and graveyard =====

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        harness.castSorcery(player1, 0, List.of(bf.get(0).getId(), bf.get(1).getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Arc Trail goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        harness.castSorcery(player1, 0, List.of(bf.get(0).getId(), bf.get(1).getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Arc Trail"));
    }
}
