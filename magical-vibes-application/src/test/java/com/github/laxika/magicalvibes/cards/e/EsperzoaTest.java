package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.k.Kaleidostone;
import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Esperzoa.class, Kaleidostone.class, CanyonMinotaur.class})
class EsperzoaTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new Esperzoa());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getDescription()).contains("Esperzoa's upkeep ability");
    }

    @Test
    @DisplayName("Prompt only includes artifacts you control")
    void promptOnlyIncludesArtifactsYouControl() {
        Permanent esperzoa = addCreatureReady(player1, new Esperzoa());
        Permanent artifact = addCreatureReady(player1, new Kaleidostone());
        Permanent nonArtifactCreature = addCreatureReady(player1, new CanyonMinotaur());
        Permanent opponentsArtifact = addCreatureReady(player2, new Kaleidostone());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(esperzoa.getId(), artifact.getId())
                .doesNotContain(nonArtifactCreature.getId())
                .doesNotContain(opponentsArtifact.getId());
    }

    @Test
    @DisplayName("Can choose itself when it is the only artifact")
    void canChooseItselfWhenOnlyArtifact() {
        Permanent esperzoa = addCreatureReady(player1, new Esperzoa());
        addCreatureReady(player1, new CanyonMinotaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(esperzoa.getId());
    }

    @Test
    @DisplayName("Returns itself when chosen")
    void returnsItselfWhenChosen() {
        Permanent esperzoa = addCreatureReady(player1, new Esperzoa());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, esperzoa.getId());

        harness.assertNotOnBattlefield(player1, "Esperzoa");
        harness.assertInHand(player1, "Esperzoa");
    }

    @Test
    @DisplayName("Returns an artifact to its owner even when controlled by another player")
    void returnsOpponentOwnedArtifactToOwner() {
        harness.addToBattlefield(player1, new Esperzoa());
        Kaleidostone card = new Kaleidostone();
        card.setOwnerId(player2.getId());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, card);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Kaleidostone");
        harness.assertInHand(player2, "Kaleidostone");
        harness.assertNotInHand(player1, "Kaleidostone");
        harness.assertOnBattlefield(player1, "Esperzoa");
    }

    @Test
    @DisplayName("Upkeep ability still returns another artifact after Esperzoa leaves")
    void resolvesAfterSourceLeaves() {
        Permanent esperzoa = harness.addToBattlefieldAndReturn(player1, new Esperzoa());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Kaleidostone());

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, esperzoa));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.assertInHand(player1, "Kaleidostone");
        harness.assertNotOnBattlefield(player1, "Kaleidostone");
    }

    @Test
    @DisplayName("Does nothing if no artifacts remain when the upkeep ability resolves")
    void doesNothingWhenNoArtifactsRemain() {
        Permanent esperzoa = harness.addToBattlefieldAndReturn(player1, new Esperzoa());
        harness.addToBattlefield(player1, new CanyonMinotaur());
        harness.addToBattlefield(player2, new Kaleidostone());

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, esperzoa));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Canyon Minotaur");
        harness.assertOnBattlefield(player2, "Kaleidostone");
    }

    @Test
    @DisplayName("Chosen artifact is returned to owner's hand")
    void chosenArtifactReturnedToOwnersHand() {
        addCreatureReady(player1, new Esperzoa());
        Permanent artifact = addCreatureReady(player1, new Kaleidostone());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
        harness.assertInHand(player1, "Kaleidostone");
    }
}
