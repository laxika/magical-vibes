package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StarOfExtinctionTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct effects: DestroyTargetPermanentEffect and MassDamageEffect with planeswalker damage")
    void hasCorrectEffects() {
        StarOfExtinction card = new StarOfExtinction();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(DestroyTargetPermanentEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1))
                .isInstanceOf(MassDamageEffect.class);

        MassDamageEffect massDamage = (MassDamageEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(massDamage.damage()).isEqualTo(20);
        assertThat(massDamage.damagesPlaneswalkers()).isTrue();
        assertThat(massDamage.damagesPlayers()).isFalse();
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting targets a land and puts spell on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Star of Extinction");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    // ===== Target validation =====

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvingDestroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Deals 20 damage to creatures on both sides, killing them")
    void deals20DamageToAllCreatures() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player1, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Both bears should be dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Bears should be in graveyards
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Deals 20 damage to planeswalkers, killing them")
    void deals20DamageToPlaneswalkers() {
        harness.addToBattlefield(player1, new Forest());
        Permanent chandra = new Permanent(new ChandraNalaar());
        chandra.setLoyaltyCounters(6);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(chandra);
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player1, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Chandra Nalaar"));
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDealDamageToPlayers() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not damage non-creature non-planeswalker permanents (e.g. lands)")
    void doesNotDamageOtherPermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player1, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Opponent's island should survive (not a creature or planeswalker)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Island"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target land is removed — no damage dealt")
    void fizzlesIfTargetLandRemoved() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);

        // Remove target land before resolution
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Forest"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature should survive because the spell fizzled
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Cleanup =====

    @Test
    @DisplayName("Star of Extinction goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Star of Extinction"));
    }
}
