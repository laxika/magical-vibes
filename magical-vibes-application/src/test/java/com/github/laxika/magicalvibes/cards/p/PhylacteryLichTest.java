package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.e.ElixirOfImmortality;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.r.RiseFromTheGrave;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({PhylacteryLich.class, Ornithopter.class, Naturalize.class, RuneclawBear.class,
        ElixirOfImmortality.class, RiseFromTheGrave.class, LightningBolt.class})
class PhylacteryLichTest extends BaseCardTest {

    @Test
    @DisplayName("Indestructible prevents lethal damage from destroying the Lich")
    void survivesLethalDamage() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0, harness.getPermanentId(player1, "Ornithopter"));
        harness.passBothPriorities();

        UUID lichId = harness.getPermanentId(player1, "Phylactery Lich");
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, lichId);
        harness.castAndResolveInstant(player2, 0, lichId);

        harness.assertOnBattlefield(player1, "Phylactery Lich");
        harness.assertNotInGraveyard(player1, "Phylactery Lich");
    }

    @Test
    @DisplayName("An artifact destroyed before resolution does not lock the Lich's entry choice")
    void canChooseRemainingArtifactAfterResponse() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new ElixirOfImmortality());
        UUID firstId = harness.getPermanentId(player1, "Ornithopter");
        UUID remainingId = harness.getPermanentId(player1, "Elixir of Immortality");
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0, firstId);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, firstId);
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.passBothPriorities();

        if (gd.interaction.isAwaitingInput()) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, remainingId);
        }

        assertThat(findPermanent(player1, "Elixir of Immortality").getCounterCount(CounterType.PHYLACTERY)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Phylactery Lich");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The artifact is chosen during resolution, not when the Lich is cast")
    void choosesArtifactAsItEnters() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new ElixirOfImmortality());
        UUID artifactId = harness.getPermanentId(player1, "Ornithopter");
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Ornithopter").getCounterCount(CounterType.PHYLACTERY)).isZero();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifactId);

        assertThat(findPermanent(player1, "Ornithopter").getCounterCount(CounterType.PHYLACTERY)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Phylactery Lich");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning the Lich from a graveyard also places its counter as it enters")
    void reanimationPlacesCounterBeforeStateTrigger() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new ElixirOfImmortality());
        UUID artifactId = harness.getPermanentId(player1, "Ornithopter");
        harness.setGraveyard(player1, List.of(new PhylacteryLich()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifactId);

        assertThat(findPermanent(player1, "Ornithopter").getCounterCount(CounterType.PHYLACTERY)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Phylactery Lich");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A nonartifact permanent with a phylactery counter keeps the Lich alive")
    void nonartifactWithCounterPreventsSacrifice() {
        harness.addToBattlefield(player1, new RuneclawBear());
        findPermanent(player1, "Runeclaw Bear").setCounterCount(CounterType.PHYLACTERY, 1);
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phylactery Lich");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Restoring a phylactery counter does not stop an already-triggered sacrifice")
    void sacrificeStillResolvesAfterCounterIsRestored() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        findPermanent(player1, "Runeclaw Bear").setCounterCount(CounterType.PHYLACTERY, 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phylactery Lich");
        harness.assertInGraveyard(player1, "Phylactery Lich");
    }


    @Test
    @DisplayName("Casting Phylactery Lich places a phylactery counter on chosen artifact")
    void castingPlacesPhylacteryCounterOnTargetArtifact() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID artifactId = harness.getPermanentId(player1, "Ornithopter");
        harness.castCreature(player1, 0, 0, artifactId);

        // Resolve creature spell — phylactery counter is placed as replacement effect
        harness.passBothPriorities();

        Permanent artifact = gqs.findPermanentById(gd, artifactId);
        assertThat(artifact.getCounterCount(CounterType.PHYLACTERY)).isEqualTo(1);

        // Lich should be on the battlefield
        harness.assertOnBattlefield(player1, "Phylactery Lich");
    }

    @Test
    @DisplayName("Phylactery counter does not interfere with charge counters")
    void phylacteryCounterDoesNotInterfereWithChargeCounters() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).getFirst();
        artifact.setCounterCount(CounterType.CHARGE, 3);

        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID artifactId = artifact.getId();
        harness.castCreature(player1, 0, 0, artifactId);
        harness.passBothPriorities();

        Permanent updatedArtifact = gqs.findPermanentById(gd, artifactId);
        assertThat(updatedArtifact.getCounterCount(CounterType.PHYLACTERY)).isEqualTo(1);
        assertThat(updatedArtifact.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }


    @Test
    @DisplayName("Choosing opponent's artifact is ignored — no counter placed, Lich sacrificed via state trigger")
    void choosingOpponentArtifactIsIgnored() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID opponentArtifactId = harness.getPermanentId(player2, "Ornithopter");
        harness.castCreature(player1, 0, 0, opponentArtifactId);
        harness.passBothPriorities(); // resolve creature spell → state trigger fires

        // Counter should NOT be placed on opponent's artifact
        Permanent artifact = gqs.findPermanentById(gd, opponentArtifactId);
        assertThat(artifact.getCounterCount(CounterType.PHYLACTERY)).isEqualTo(0);

        // State trigger is on the stack — Lich is still alive
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.assertOnBattlefield(player1, "Phylactery Lich");

        // Resolve state trigger → Lich is sacrificed
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Phylactery Lich");
        harness.assertInGraveyard(player1, "Phylactery Lich");
    }

    @Test
    @DisplayName("Choosing a non-artifact permanent is ignored — no counter placed, Lich sacrificed via state trigger")
    void choosingNonArtifactPermanentIsIgnored() {
        harness.addToBattlefield(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID creatureId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castCreature(player1, 0, 0, creatureId);
        resolveAllTriggers();

        // Counter should NOT be placed on a non-artifact
        Permanent perm = gqs.findPermanentById(gd, creatureId);
        assertThat(perm.getCounterCount(CounterType.PHYLACTERY)).isEqualTo(0);

        // Lich should be in graveyard
        harness.assertNotOnBattlefield(player1, "Phylactery Lich");
        harness.assertInGraveyard(player1, "Phylactery Lich");
    }

    @Test
    @DisplayName("Casting without artifacts — state trigger fires, Lich sacrificed after resolution")
    void castWithoutArtifacts() {
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        // Cast without choosing any artifact (no target)
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Phylactery Lich");
        harness.assertInGraveyard(player1, "Phylactery Lich");
    }


    @Test
    @DisplayName("State trigger goes on the stack and Lich survives until it resolves")
    void stateTriggerGoesOnStack() {
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → state trigger fires

        // Lich is on the battlefield while trigger is on the stack
        harness.assertOnBattlefield(player1, "Phylactery Lich");

        // State trigger is on the stack
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getDescription().contains("Phylactery Lich"));

        // Resolve trigger → Lich is sacrificed
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Phylactery Lich");
    }

    @Test
    @DisplayName("Phylactery Lich is sacrificed when artifact with counter is destroyed by a spell")
    void sacrificedWhenArtifactWithCounterIsDestroyed() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID artifactId = harness.getPermanentId(player1, "Ornithopter");
        harness.castCreature(player1, 0, 0, artifactId);
        harness.passBothPriorities();

        // Verify setup
        harness.assertOnBattlefield(player1, "Phylactery Lich");
        assertThat(gqs.findPermanentById(gd, artifactId).getCounterCount(CounterType.PHYLACTERY)).isEqualTo(1);

        // Opponent casts Naturalize to destroy the artifact
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, artifactId);

        // State trigger is now on the stack — resolve it
        harness.passBothPriorities();

        // Artifact destroyed, state trigger resolved → Lich is sacrificed
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Phylactery Lich");
        harness.assertInGraveyard(player1, "Phylactery Lich");
    }

    @Test
    @DisplayName("Phylactery Lich survives while artifact with counter remains")
    void survivesWhileArtifactWithCounterRemains() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID artifactId = harness.getPermanentId(player1, "Ornithopter");
        harness.castCreature(player1, 0, 0, artifactId);
        harness.passBothPriorities();

        // Lich and artifact should both be on the battlefield
        harness.assertOnBattlefield(player1, "Phylactery Lich");
        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gqs.findPermanentById(gd, artifactId).getCounterCount(CounterType.PHYLACTERY)).isEqualTo(1);

        // No state trigger should fire
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Phylactery Lich survives if another artifact has phylactery counters")
    void survivesIfAnotherArtifactHasPhylacteryCounters() {
        // Place two artifacts
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new ElixirOfImmortality());

        // Manually put a phylactery counter on the second artifact
        Permanent secondPerm = findPermanent(player1, "Elixir of Immortality");
        secondPerm.setCounterCount(CounterType.PHYLACTERY, 1);

        // Cast Lich choosing the first artifact
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        UUID firstArtifactId = harness.getPermanentId(player1, "Ornithopter");
        harness.castCreature(player1, 0, 0, firstArtifactId);
        harness.passBothPriorities();

        // Destroy Ornithopter (first artifact) with Naturalize
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, firstArtifactId);

        // Lich should survive — second artifact still has phylactery counters
        // No state trigger should fire
        harness.assertOnBattlefield(player1, "Phylactery Lich");
    }

    @Test
    @DisplayName("Phylactery Lich is sacrificed despite being indestructible")
    void sacrificedDespiteIndestructible() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new PhylacteryLich()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID artifactId = harness.getPermanentId(player1, "Ornithopter");
        harness.castCreature(player1, 0, 0, artifactId);
        harness.passBothPriorities();

        // Lich is on battlefield and has Indestructible (from Scryfall)
        harness.assertOnBattlefield(player1, "Phylactery Lich");

        // Destroy the artifact via Naturalize
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, artifactId);

        // Resolve state trigger
        harness.passBothPriorities();

        // Indestructible does NOT prevent sacrifice — Lich goes to graveyard
        harness.assertNotOnBattlefield(player1, "Phylactery Lich");
        harness.assertInGraveyard(player1, "Phylactery Lich");
    }
}
