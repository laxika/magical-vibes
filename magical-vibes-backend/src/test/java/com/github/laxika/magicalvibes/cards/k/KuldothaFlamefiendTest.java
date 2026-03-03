package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KuldothaFlamefiendTest extends BaseCardTest {

    @Test
    void happyPath_sacrificeArtifactDealsFourDamageToCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Add artifact to sacrifice
        harness.addToBattlefield(player1, new Ornithopter());
        UUID ornithopterId = harness.getPermanentId(player1, "Ornithopter");

        // Add target creature (Grizzly Bears is 2/2)
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Set damage assignments before casting
        gd.pendingETBDamageAssignments = Map.of(bearsId, 4);

        harness.setHand(player1, List.of(new KuldothaFlamefiend()));
        harness.addMana(player1, ManaColor.RED, 6);

        // Cast creature
        harness.castCreature(player1, 0);

        // Resolve creature spell → MayEffect queues may prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Accept sacrifice
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose Ornithopter to sacrifice
        harness.handlePermanentChosen(player1, ornithopterId);

        // Verify: Ornithopter sacrificed, Grizzly Bears destroyed
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void declineSacrifice_noSacrificeNoDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        gd.pendingETBDamageAssignments = Map.of(bearsId, 4);

        harness.setHand(player1, List.of(new KuldothaFlamefiend()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Decline sacrifice
        harness.handleMayAbilityChosen(player1, false);

        // Ornithopter still on battlefield, Bears still alive
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void noArtifacts_acceptDoesNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // No artifacts on player1's battlefield
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        gd.pendingETBDamageAssignments = Map.of(bearsId, 4);

        harness.setHand(player1, List.of(new KuldothaFlamefiend()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Accept sacrifice — but no artifacts to sacrifice, so nothing happens
        harness.handleMayAbilityChosen(player1, true);

        // Grizzly Bears still alive, Flamefiend on battlefield
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Kuldotha Flamefiend");
    }

    @Test
    void splitDamage_twoCreaturesGetTwoDamageEach() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Ornithopter());
        UUID ornithopterId = harness.getPermanentId(player1, "Ornithopter");

        // Add two target creatures (Ornithopter is 0/2, so 2 damage kills it)
        harness.addToBattlefield(player2, new Ornithopter());
        UUID target1Id = gd.playerBattlefields.get(player2.getId()).getLast().getId();

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID target2Id = harness.getPermanentId(player2, "Grizzly Bears");

        gd.pendingETBDamageAssignments = Map.of(target1Id, 2, target2Id, 2);

        harness.setHand(player1, List.of(new KuldothaFlamefiend()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may prompt

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ornithopterId);

        // Both targets should be destroyed (Ornithopter 0/2, Grizzly Bears 2/2 — both die to 2 damage)
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void playerTarget_damageDealtToPlayer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Ornithopter());
        UUID ornithopterId = harness.getPermanentId(player1, "Ornithopter");

        // Target player2 directly
        gd.pendingETBDamageAssignments = Map.of(player2.getId(), 4);

        harness.setHand(player1, List.of(new KuldothaFlamefiend()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may prompt

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ornithopterId);

        harness.assertLife(player2, 16); // 20 - 4 = 16
    }

    @Test
    void playerAndCreatureTargets_mixedDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Ornithopter());
        UUID ornithopterId = harness.getPermanentId(player1, "Ornithopter");

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Split: 2 to creature, 2 to player
        gd.pendingETBDamageAssignments = Map.of(bearsId, 2, player2.getId(), 2);

        harness.setHand(player1, List.of(new KuldothaFlamefiend()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may prompt

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ornithopterId);

        // Bears killed (2/2 takes 2 damage), player takes 2
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 18); // 20 - 2 = 18
    }

    @Test
    void targetRemoved_damageToRemovedTargetSkipped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Ornithopter());
        UUID ornithopterId = harness.getPermanentId(player1, "Ornithopter");

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // One target exists, one doesn't
        UUID nonexistentTarget = UUID.randomUUID();
        gd.pendingETBDamageAssignments = Map.of(bearsId, 2, nonexistentTarget, 2);

        harness.setHand(player1, List.of(new KuldothaFlamefiend()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may prompt

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ornithopterId);

        // Bears killed by its 2 damage, nonexistent target damage simply skipped
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20); // no damage to player
    }

    @Test
    void noDamageAssignments_sacrificeHappensButNoDamageDealt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Ornithopter());
        UUID ornithopterId = harness.getPermanentId(player1, "Ornithopter");

        // Empty damage assignments
        gd.pendingETBDamageAssignments = Map.of();

        harness.setHand(player1, List.of(new KuldothaFlamefiend()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may prompt

        // May prompt should still appear (controller has artifacts)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ornithopterId);

        // Artifact sacrificed but no damage dealt
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertLife(player2, 20);
    }

    @Test
    void multipleArtifacts_playerChoosesWhichToSacrifice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Add two different artifacts
        harness.addToBattlefield(player1, new Ornithopter());
        UUID ornithopter1Id = gd.playerBattlefields.get(player1.getId()).getLast().getId();

        harness.addToBattlefield(player1, new Ornithopter());
        UUID ornithopter2Id = gd.playerBattlefields.get(player1.getId()).getLast().getId();

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        gd.pendingETBDamageAssignments = Map.of(bearsId, 4);

        harness.setHand(player1, List.of(new KuldothaFlamefiend()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may prompt

        harness.handleMayAbilityChosen(player1, true);

        // Player picks the second Ornithopter to sacrifice
        harness.handlePermanentChosen(player1, ornithopter2Id);

        // Bears killed, first Ornithopter stays, second gone
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // One Ornithopter remains (checking count)
        long ornithopterCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ornithopter"))
                .count();
        assertThat(ornithopterCount).isEqualTo(1);

        // The sacrificed one is the second one
        Permanent remainingOrnithopter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ornithopter"))
                .findFirst().orElse(null);
        assertThat(remainingOrnithopter).isNotNull();
        assertThat(remainingOrnithopter.getId()).isEqualTo(ornithopter1Id);
    }
}
