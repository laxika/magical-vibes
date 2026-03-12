package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.t.TheHive;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutPhylacteryCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PhylacteryLichTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Phylactery Lich has non-targeting ETB phylactery counter effect and state trigger")
    void hasCorrectAbilityStructure() {
        PhylacteryLich card = new PhylacteryLich();

        // Per MTG rulings: "Phylactery Lich's first ability doesn't target the artifact."
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(PutPhylacteryCounterOnTargetPermanentEffect.class);

        // State-triggered sacrifice ability (rule 603.8)
        assertThat(card.getEffects(EffectSlot.STATE_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATE_TRIGGERED).getFirst())
                .isInstanceOf(StateTriggerEffect.class);
    }

    // ===== ETB phylactery counter placement =====

    @Test
    @DisplayName("Casting Phylactery Lich places a phylactery counter on chosen artifact")
    void castingPlacesPhylacteryCounterOnTargetArtifact() {
        harness.addToBattlefield(player1, new TheHive());
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID artifactId = harness.getPermanentId(player1, "The Hive");
        harness.castCreature(player1, 0, 0, artifactId);

        // Resolve creature spell — phylactery counter is placed as replacement effect
        harness.passBothPriorities();

        Permanent artifact = gqs.findPermanentById(gd, artifactId);
        assertThat(artifact.getPhylacteryCounters()).isEqualTo(1);

        // Lich should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phylactery Lich"));
    }

    @Test
    @DisplayName("Phylactery counter does not interfere with charge counters")
    void phylacteryCounterDoesNotInterfereWithChargeCounters() {
        harness.addToBattlefield(player1, new TheHive());
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).getFirst();
        artifact.setChargeCounters(3);

        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID artifactId = artifact.getId();
        harness.castCreature(player1, 0, 0, artifactId);
        harness.passBothPriorities();

        Permanent updatedArtifact = gqs.findPermanentById(gd, artifactId);
        assertThat(updatedArtifact.getPhylacteryCounters()).isEqualTo(1);
        assertThat(updatedArtifact.getChargeCounters()).isEqualTo(3);
    }

    // ===== Choice restriction (does not target) =====

    @Test
    @DisplayName("Choosing opponent's artifact is ignored — no counter placed, Lich sacrificed via state trigger")
    void choosingOpponentArtifactIsIgnored() {
        harness.addToBattlefield(player2, new TheHive());
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID opponentArtifactId = harness.getPermanentId(player2, "The Hive");
        harness.castCreature(player1, 0, 0, opponentArtifactId);
        harness.passBothPriorities(); // resolve creature spell → state trigger fires

        // Counter should NOT be placed on opponent's artifact
        Permanent artifact = gqs.findPermanentById(gd, opponentArtifactId);
        assertThat(artifact.getPhylacteryCounters()).isEqualTo(0);

        // State trigger is on the stack — Lich is still alive
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phylactery Lich"));

        // Resolve state trigger → Lich is sacrificed
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phylactery Lich"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Phylactery Lich"));
    }

    @Test
    @DisplayName("Choosing a non-artifact permanent is ignored — no counter placed, Lich sacrificed via state trigger")
    void choosingNonArtifactPermanentIsIgnored() {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setPower(2);
        creature.setToughness(2);
        creature.setManaCost("{1}{G}");
        harness.addToBattlefield(player1, creature);

        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID creatureId = harness.getPermanentId(player1, "Test Creature");
        harness.castCreature(player1, 0, 0, creatureId);
        harness.passBothPriorities(); // resolve creature spell → state trigger fires
        harness.passBothPriorities(); // resolve state trigger → Lich sacrificed

        // Counter should NOT be placed on a non-artifact
        Permanent perm = gqs.findPermanentById(gd, creatureId);
        assertThat(perm.getPhylacteryCounters()).isEqualTo(0);

        // Lich should be in graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phylactery Lich"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Phylactery Lich"));
    }

    @Test
    @DisplayName("Casting without artifacts — state trigger fires, Lich sacrificed after resolution")
    void castWithoutArtifacts() {
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        // Cast without choosing any artifact (no target)
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → state trigger fires
        harness.passBothPriorities(); // resolve state trigger → Lich sacrificed

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phylactery Lich"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Phylactery Lich"));
    }

    // ===== State-triggered sacrifice (rule 603.8) =====

    @Test
    @DisplayName("State trigger goes on the stack and Lich survives until it resolves")
    void stateTriggerGoesOnStack() {
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → state trigger fires

        // Lich is on the battlefield while trigger is on the stack
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phylactery Lich"));

        // State trigger is on the stack
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getDescription().contains("Phylactery Lich"));

        // Resolve trigger → Lich is sacrificed
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phylactery Lich"));
    }

    @Test
    @DisplayName("Phylactery Lich is sacrificed when artifact with counter is destroyed by a spell")
    void sacrificedWhenArtifactWithCounterIsDestroyed() {
        harness.addToBattlefield(player1, new TheHive());
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID artifactId = harness.getPermanentId(player1, "The Hive");
        harness.castCreature(player1, 0, 0, artifactId);
        harness.passBothPriorities();

        // Verify setup
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phylactery Lich"));
        assertThat(gqs.findPermanentById(gd, artifactId).getPhylacteryCounters()).isEqualTo(1);

        // Opponent casts Shatter to destroy the artifact
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, artifactId);

        // State trigger is now on the stack — resolve it
        harness.passBothPriorities();

        // Artifact destroyed, state trigger resolved → Lich is sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("The Hive"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phylactery Lich"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Phylactery Lich"));
    }

    @Test
    @DisplayName("Phylactery Lich survives while artifact with counter remains")
    void survivesWhileArtifactWithCounterRemains() {
        harness.addToBattlefield(player1, new TheHive());
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID artifactId = harness.getPermanentId(player1, "The Hive");
        harness.castCreature(player1, 0, 0, artifactId);
        harness.passBothPriorities();

        // Lich and artifact should both be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phylactery Lich"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("The Hive"));
        assertThat(gqs.findPermanentById(gd, artifactId).getPhylacteryCounters()).isEqualTo(1);

        // No state trigger should fire
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Phylactery Lich survives if another artifact has phylactery counters")
    void survivesIfAnotherArtifactHasPhylacteryCounters() {
        // Place two artifacts
        harness.addToBattlefield(player1, new TheHive());
        Card secondArtifact = new Card();
        secondArtifact.setName("Second Artifact");
        secondArtifact.setType(CardType.ARTIFACT);
        secondArtifact.setManaCost("{2}");
        harness.addToBattlefield(player1, secondArtifact);

        // Manually put a phylactery counter on the second artifact
        Permanent secondPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Second Artifact"))
                .findFirst().orElseThrow();
        secondPerm.setPhylacteryCounters(1);

        // Cast Lich choosing the first artifact
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        UUID firstArtifactId = harness.getPermanentId(player1, "The Hive");
        harness.castCreature(player1, 0, 0, firstArtifactId);
        harness.passBothPriorities();

        // Destroy The Hive (first artifact) with Shatter
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, firstArtifactId);

        // Lich should survive — second artifact still has phylactery counters
        // No state trigger should fire
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phylactery Lich"));
    }

    @Test
    @DisplayName("Phylactery Lich is sacrificed despite being indestructible")
    void sacrificedDespiteIndestructible() {
        harness.addToBattlefield(player1, new TheHive());
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID artifactId = harness.getPermanentId(player1, "The Hive");
        harness.castCreature(player1, 0, 0, artifactId);
        harness.passBothPriorities();

        // Lich is on battlefield and has Indestructible (from Scryfall)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phylactery Lich"));

        // Destroy the artifact via Shatter
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, artifactId);

        // Resolve state trigger
        harness.passBothPriorities();

        // Indestructible does NOT prevent sacrifice — Lich goes to graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phylactery Lich"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Phylactery Lich"));
    }
}
