package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToSecondaryTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinBarrageTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has sacrifice kicker, creature damage, and kicked player damage effects")
    void hasCorrectEffects() {
        GoblinBarrage card = new GoblinBarrage();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof KickerEffect ke && ke.hasSacrificeCost() && !ke.hasManaCost());

        assertThat(card.getEffects(EffectSlot.SPELL))
                .hasSize(2)
                .anySatisfy(e -> {
                    assertThat(e).isInstanceOf(DealDamageToTargetCreatureEffect.class);
                    assertThat(((DealDamageToTargetCreatureEffect) e).damage()).isEqualTo(4);
                })
                .anySatisfy(e -> {
                    assertThat(e).isInstanceOf(KickedConditionalEffect.class);
                    assertThat(((KickedConditionalEffect) e).wrapped()).isInstanceOf(DealDamageToSecondaryTargetEffect.class);
                    assertThat(((DealDamageToSecondaryTargetEffect) ((KickedConditionalEffect) e).wrapped()).damage()).isEqualTo(4);
                });
    }

    // ===== Cast without kicker =====

    @Test
    @DisplayName("Without kicker — deals 4 damage to target creature")
    void deals4DamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3); // 3 generic + 1 red

        harness.castSorceryWithSacrifice(player1, 0, targetId, null);
        harness.passBothPriorities();

        // Grizzly Bears is 2/2, 4 damage kills it
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without kicker — does not deal damage to player")
    void doesNotDealDamageToPlayerWithoutKicker() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorceryWithSacrifice(player1, 0, targetId, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Cast with kicker =====

    @Test
    @DisplayName("With kicker (sacrifice artifact) — deals 4 damage to creature and 4 to player")
    void kickedWithArtifactDeals4ToCreatureAnd4ToPlayer() {
        harness.addToBattlefield(player1, new Spellbook());
        UUID artifactId = harness.getPermanentId(player1, "Spellbook");
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureTarget = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedSorceryWithSacrifice(player1, 0, creatureTarget, player2.getId(), artifactId);
        harness.passBothPriorities();

        // Creature takes 4 damage (Grizzly Bears is 2/2 → dead)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Player takes 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // Artifact was sacrificed
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("With kicker (sacrifice Goblin) — deals 4 damage to creature and 4 to player")
    void kickedWithGoblinDeals4ToCreatureAnd4ToPlayer() {
        harness.addToBattlefield(player1, new GoblinPiker());
        UUID goblinId = harness.getPermanentId(player1, "Goblin Piker");
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureTarget = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedSorceryWithSacrifice(player1, 0, creatureTarget, player2.getId(), goblinId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertNotOnBattlefield(player1, "Goblin Piker");
        harness.assertInGraveyard(player1, "Goblin Piker");
    }

    // ===== Kicker cost validation =====

    @Test
    @DisplayName("Cannot kick without a valid artifact or Goblin to sacrifice")
    void cannotKickWithoutValidSacrifice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureTarget = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, creatureTarget, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-artifact non-Goblin for kicker")
    void cannotSacrificeNonArtifactNonGoblin() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // Bears are not Goblins or artifacts
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new GoblinPiker()); // target creature
        UUID targetCreature = harness.getPermanentId(player2, "Goblin Piker");
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrifice(player1, 0, targetCreature, player2.getId(), bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("an artifact or Goblin");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Without kicker — fizzles if creature target is removed before resolution")
    void fizzlesIfCreatureRemovedWithoutKicker() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorceryWithSacrifice(player1, 0, targetId, null);

        // Remove creature before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With kicker — still deals damage to player if creature target is removed before resolution")
    void kickedStillDamagesPlayerIfCreatureRemoved() {
        harness.addToBattlefield(player1, new Spellbook());
        UUID artifactId = harness.getPermanentId(player1, "Spellbook");
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureTarget = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedSorceryWithSacrifice(player1, 0, creatureTarget, player2.getId(), artifactId);

        // Remove creature before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Spell should NOT fizzle — player target is still valid
        assertThat(gd.gameLog).noneMatch(log -> log.contains("fizzles"));
        // Player still takes 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== Spell goes to graveyard =====

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorceryWithSacrifice(player1, 0, targetId, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Goblin Barrage");
    }
}
