package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Equipoise.class, Forest.class, GrizzlyBears.class, HillGiant.class, IronStar.class,
        Island.class, Juggernaut.class, Millstone.class, Plains.class})
class EquipoiseTest extends BaseCardTest {

    private void resolveUpkeepTargetingOpponent() {
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Excess lands of the target phase out; equal land counts do nothing")
    void excessLandsPhaseOut() {
        harness.addToBattlefield(player1, new Equipoise());
        harness.addToBattlefield(player1, new Plains());
        Permanent excessIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent excessForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent keptIsland = harness.addToBattlefieldAndReturn(player2, new Island());

        resolveUpkeepTargetingOpponent();

        // player1 has 1 land, player2 has 3 → choose 2 to phase out
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(excessIsland.getId(), excessForest.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(keptIsland);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(excessIsland, excessForest);
    }

    @Test
    @DisplayName("When you control zero lands, all of the target's lands phase out automatically")
    void zeroOwnLandsPhasesAllTargetLands() {
        harness.addToBattlefield(player1, new Equipoise());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        resolveUpkeepTargetingOpponent();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(island, forest);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(island, forest);
    }

    @Test
    @DisplayName("No excess of any type leaves the board unchanged")
    void noExcessDoesNothing() {
        harness.addToBattlefield(player1, new Equipoise());
        Permanent myLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent theirLand = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent myCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveUpkeepTargetingOpponent();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(myLand, myCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(theirLand, theirCreature);
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of())).isEmpty();
    }

    @Test
    @DisplayName("An artifact creature phased during the artifact pass no longer counts for creatures")
    void artifactCreaturePhasedDuringArtifactPassReducesCreatureExcess() {
        harness.addToBattlefield(player1, new Equipoise());
        // player1: 0 artifacts, 1 creature. player2: 1 artifact creature + 1 creature.
        // Artifact excess = 1 → Juggernaut phases out. Creature excess then = 1−1 = 0.
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        resolveUpkeepTargetingOpponent();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(juggernaut);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Lands, artifacts, and creatures each run as separate excess passes")
    void allThreePassesCanPhaseOut() {
        harness.addToBattlefield(player1, new Equipoise());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveUpkeepTargetingOpponent();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(land, artifact, creature);
    }

    @Test
    @DisplayName("The controller chooses excess permanents during each type pass")
    void controllerChoosesExcessPermanentsForEachType() {
        harness.addToBattlefield(player1, new Equipoise());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new IronStar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent chosenLand = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent keptLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent chosenArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent keptArtifact = harness.addToBattlefieldAndReturn(player2, new IronStar());
        Permanent chosenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent keptCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        resolveUpkeepTargetingOpponent();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)
                .maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, List.of(chosenLand.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)
                .maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, List.of(chosenArtifact.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)
                .maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, List.of(chosenCreature.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .contains(chosenLand, chosenArtifact, chosenCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(keptLand, keptArtifact, keptCreature);
    }

    @Test
    @DisplayName("Permanents phased out by Equipoise phase in before the target's next untap")
    void phasedOutPermanentsPhaseInBeforeTargetNextUntap() {
        harness.addToBattlefield(player1, new Equipoise());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        resolveUpkeepTargetingOpponent();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(land);

        harness.passUntil(player2, TurnStep.UNTAP);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of()))
                .doesNotContain(land);
    }

    @Test
    @DisplayName("The ability triggers only during Equipoise's controller's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new Equipoise());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of()))
                .doesNotContain(land);
    }

    @Test
    @DisplayName("Targeting yourself is legal and phases nothing when counts match")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new Equipoise());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IronStar());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land, artifact);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).isEmpty();
    }
}
