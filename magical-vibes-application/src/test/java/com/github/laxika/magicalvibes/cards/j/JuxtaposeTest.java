package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JuxtaposeTest extends BaseCardTest {

    private void castJuxtapose() {
        harness.setHand(player1, List.of(new Juxtapose()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private boolean controls(UUID playerId, UUID permanentId) {
        return gd.playerBattlefields.get(playerId).stream().anyMatch(p -> p.getId().equals(permanentId));
    }

    @Test
    @DisplayName("Greatest mana value creatures exchange controllers")
    void exchangesGreatestCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castJuxtapose();

        assertThat(controls(player2.getId(), mine.getId())).isTrue();
        assertThat(controls(player1.getId(), theirs.getId())).isTrue();
    }

    @Test
    @DisplayName("Only the greatest mana value creature is exchanged; lesser creatures stay")
    void onlyGreatestCreatureMoves() {
        Permanent bigMine = harness.addToBattlefieldAndReturn(player1, new HillGiant());
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
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castJuxtapose();

        // Player1's two Hill Giants tie for greatest mana value — player1 chooses one.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(controls(player2.getId(), first.getId())).isTrue();
        assertThat(controls(player1.getId(), second.getId())).isTrue();
        assertThat(controls(player1.getId(), theirs.getId())).isTrue();
    }

    @Test
    @DisplayName("An artifact creature can be exchanged in both the creature and artifact steps")
    void artifactCreatureExchangedTwice() {
        // Juggernaut is an artifact creature and player1's greatest creature; after it leaves in the
        // creature step, IronStar becomes player1's artifact for the artifact step, which pulls
        // Juggernaut back from player2.
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player1, new Juggernaut());
        Permanent ironStar = harness.addToBattlefieldAndReturn(player1, new IronStar());
        Permanent theirCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castJuxtapose();

        assertThat(controls(player1.getId(), juggernaut.getId())).isTrue();
        assertThat(controls(player1.getId(), theirCreature.getId())).isTrue();
        assertThat(controls(player2.getId(), ironStar.getId())).isTrue();
    }
}
