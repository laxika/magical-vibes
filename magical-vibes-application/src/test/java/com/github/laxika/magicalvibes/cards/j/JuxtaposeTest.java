package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DancingScimitar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Juxtapose.class, DancingScimitar.class, GrizzlyBears.class, AirElemental.class,
        IronStar.class, Millstone.class})
class JuxtaposeTest extends BaseCardTest {

    private void castJuxtapose() {
        castJuxtapose(player2.getId());
    }

    private void castJuxtapose(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Juxtapose()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private boolean controls(UUID playerId, UUID permanentId) {
        return gd.playerBattlefields.get(playerId).stream().anyMatch(p -> p.getId().equals(permanentId));
    }

    @Test
    @DisplayName("Greatest mana value creatures exchange controllers")
    void exchangesGreatestCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castJuxtapose();

        assertThat(controls(player2.getId(), mine.getId())).isTrue();
        assertThat(controls(player1.getId(), theirs.getId())).isTrue();
    }

    @Test
    @DisplayName("Only the greatest mana value artifact is exchanged; lesser artifacts stay")
    void onlyGreatestArtifactMoves() {
        Permanent bigMine = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent smallMine = harness.addToBattlefieldAndReturn(player1, new IronStar());
        Permanent bigTheirs = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent smallTheirs = harness.addToBattlefieldAndReturn(player2, new IronStar());

        castJuxtapose();

        assertThat(controls(player2.getId(), bigMine.getId())).isTrue();
        assertThat(controls(player1.getId(), smallMine.getId())).isTrue();
        assertThat(controls(player1.getId(), bigTheirs.getId())).isTrue();
        assertThat(controls(player2.getId(), smallTheirs.getId())).isTrue();
    }

    @Test
    @DisplayName("Only the greatest mana value creature is exchanged; lesser creatures stay")
    void onlyGreatestCreatureMoves() {
        Permanent bigMine = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent smallMine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castJuxtapose();

        assertThat(controls(player2.getId(), bigMine.getId())).isTrue();
        assertThat(controls(player1.getId(), smallMine.getId())).isTrue();
        assertThat(controls(player1.getId(), theirs.getId())).isTrue();
    }

    @Test
    @DisplayName("Greatest mana value artifacts exchange controllers")
    void exchangesGreatestArtifacts() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new IronStar());

        castJuxtapose();

        assertThat(controls(player2.getId(), mine.getId())).isTrue();
        assertThat(controls(player1.getId(), theirs.getId())).isTrue();
    }

    @Test
    @DisplayName("No creature on one side skips the creature exchange but artifacts still swap")
    void missingCreatureStillSwapsArtifacts() {
        Permanent myArtifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent theirCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent theirArtifact = harness.addToBattlefieldAndReturn(player2, new IronStar());

        castJuxtapose();

        // Player1 controls no creature, so nothing is exchanged in the creature step.
        assertThat(controls(player2.getId(), theirCreature.getId())).isTrue();
        // Artifacts still swap.
        assertThat(controls(player2.getId(), myArtifact.getId())).isTrue();
        assertThat(controls(player1.getId(), theirArtifact.getId())).isTrue();
    }

    @Test
    @DisplayName("A tie for greatest lets the controller choose which permanent is exchanged")
    void tieLetsControllerChoose() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castJuxtapose();

        // Player1's two Air Elementals tie for greatest mana value — player1 chooses one.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(controls(player2.getId(), first.getId())).isTrue();
        assertThat(controls(player1.getId(), second.getId())).isTrue();
        assertThat(controls(player1.getId(), theirs.getId())).isTrue();
    }

    @Test
    @DisplayName("A tie for greatest lets the target player choose which permanent is exchanged")
    void tieLetsTargetPlayerChoose() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castJuxtapose();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, first.getId());

        assertThat(controls(player2.getId(), mine.getId())).isTrue();
        assertThat(controls(player1.getId(), first.getId())).isTrue();
        assertThat(controls(player2.getId(), second.getId())).isTrue();
    }

    @Test
    @DisplayName("Artifact ties prompt the two controllers in sequence")
    void artifactTiesPromptBothControllers() {
        Permanent firstMine = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent secondMine = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent firstTheirs = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent secondTheirs = harness.addToBattlefieldAndReturn(player2, new Millstone());

        castJuxtapose();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice controllerChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(controllerChoice.playerId()).isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, firstMine.getId());

        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.playerId()).isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, firstTheirs.getId());

        assertThat(controls(player2.getId(), firstMine.getId())).isTrue();
        assertThat(controls(player1.getId(), secondMine.getId())).isTrue();
        assertThat(controls(player1.getId(), firstTheirs.getId())).isTrue();
        assertThat(controls(player2.getId(), secondTheirs.getId())).isTrue();
    }

    @Test
    @DisplayName("An artifact exchange is skipped when only one player controls an artifact")
    void missingArtifactSkipsArtifactExchange() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castJuxtapose();

        assertThat(controls(player1.getId(), mine.getId())).isTrue();
        assertThat(controls(player2.getId(), theirs.getId())).isTrue();
    }

    @Test
    @DisplayName("Targeting yourself does not create a control change")
    void targetingYourselfDoesNothing() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castJuxtapose(player1.getId());

        assertThat(controls(player1.getId(), mine.getId())).isTrue();
        assertThat(controls(player2.getId(), theirs.getId())).isTrue();
    }

    @Test
    @DisplayName("Face-down permanents have mana value zero when choosing the greatest creature")
    void faceDownCreatureUsesCurrentManaValue() {
        Permanent greatestCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castJuxtapose();

        assertThat(controls(player2.getId(), greatestCreature.getId())).isTrue();
        assertThat(controls(player1.getId(), theirs.getId())).isTrue();
        assertThat(controls(player1.getId(), faceDownCreature.getId())).isTrue();
    }

    @Test
    @DisplayName("An artifact creature can be exchanged in both the creature and artifact steps")
    void artifactCreatureExchangedTwice() {
        // Dancing Scimitar is an artifact creature and player1's greatest creature; after it leaves
        // in the creature step, Iron Star becomes player1's artifact for the artifact step, which
        // pulls Dancing Scimitar back from player2.
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new DancingScimitar());
        Permanent ironStar = harness.addToBattlefieldAndReturn(player1, new IronStar());
        Permanent theirCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castJuxtapose();

        assertThat(controls(player1.getId(), artifactCreature.getId())).isTrue();
        assertThat(controls(player1.getId(), theirCreature.getId())).isTrue();
        assertThat(controls(player2.getId(), ironStar.getId())).isTrue();
    }
}
