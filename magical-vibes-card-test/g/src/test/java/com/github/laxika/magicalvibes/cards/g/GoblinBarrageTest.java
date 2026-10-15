package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.j.JayaBallard;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinBarrage.class, GrizzlyBears.class, GoblinPiker.class, Spellbook.class, JayaBallard.class})
class GoblinBarrageTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker — deals 4 damage to target creature")
    void deals4DamageToCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorceryWithSacrifice(player1, 0, targetId, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With kicker (sacrifice artifact) — deals 4 damage to creature and 4 to player")
    void kickedWithArtifactDeals4ToCreatureAnd4ToPlayer() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, new Spellbook()).getId();
        UUID creatureTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
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
        UUID goblinId = harness.addToBattlefieldAndReturn(player1, new GoblinPiker()).getId();
        UUID creatureTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
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

    @Test
    @DisplayName("Cannot kick without a valid artifact or Goblin to sacrifice")
    void cannotKickWithoutValidSacrifice() {
        UUID creatureTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
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
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID targetCreature = harness.addToBattlefieldAndReturn(player2, new GoblinPiker()).getId();
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrifice(player1, 0, targetCreature, player2.getId(), bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("an artifact or Goblin");
    }

    @Test
    @DisplayName("Without kicker — fizzles if creature target is removed before resolution")
    void fizzlesIfCreatureRemovedWithoutKicker() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorceryWithSacrifice(player1, 0, targetId, null);

        // Remove creature before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With kicker — still deals damage to player if creature target is removed before resolution")
    void kickedStillDamagesPlayerIfCreatureRemoved() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, new Spellbook()).getId();
        UUID creatureTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedSorceryWithSacrifice(player1, 0, creatureTarget, player2.getId(), artifactId);

        // Remove creature before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Spell should NOT fizzle — player target is still valid
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains("fizzles"));
        // Player still takes 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void kickedDealsFourDamageToPlaneswalker() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, new Spellbook()).getId();
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        var planeswalker = harness.addToBattlefieldAndReturn(player2, new JayaBallard());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castKickedSorceryWithSacrifice(player1, 0, creatureId, planeswalker.getId(), artifactId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void canSacrificeTheTargetedGoblinToKick() {
        UUID goblinId = harness.addToBattlefieldAndReturn(player1, new GoblinPiker()).getId();
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castKickedSorceryWithSacrifice(player1, 0, goblinId, player2.getId(), goblinId);
        harness.assertInGraveyard(player1, "Goblin Piker");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Goblin Barrage");
    }

    @Test
    void cannotChooseCreatureAsKickedDamageTarget() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, new Spellbook()).getId();
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        UUID otherCreatureId = harness.addToBattlefieldAndReturn(player2, new GoblinPiker()).getId();
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrifice(
                player1, 0, creatureId, otherCreatureId, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotKickWithoutPlayerOrPlaneswalkerTarget() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, new Spellbook()).getId();
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrificeNoKickerTarget(
                player1, 0, creatureId, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new GoblinBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorceryWithSacrifice(player1, 0, targetId, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Goblin Barrage");
    }
}
