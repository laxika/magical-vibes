package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelPlate;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcolyteHybrid.class, DarksteelPlate.class, FountainOfYouth.class, GrizzlyBears.class})
class AcolyteHybridTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking destroys an artifact and its controller draws a card")
    void attackingDestroysArtifactAndItsControllerDraws() {
        addAttacker();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Choosing zero targets leaves the artifact alone")
    void choosingZeroTargetsDoesNothing() {
        addAttacker();
        harness.addToBattlefield(player2, new FountainOfYouth());
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The attack trigger only allows artifact targets")
    void onlyArtifactsAreLegalTargets() {
        addAttacker();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(artifact.getId()).doesNotContain(creature.getId());
    }

    @Test
    @DisplayName("An indestructible artifact is not destroyed and does not cause a draw")
    void indestructibleArtifactDoesNotCauseDraw() {
        addAttacker();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelPlate());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Plate");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Attacking with no artifacts still puts the zero-target trigger on the stack")
    void attackingWithoutArtifactsStillTriggers() {
        addAttacker();

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Destroying your own artifact draws for you rather than the defending player")
    void ownArtifactDrawsForAttackingPlayer() {
        addAttacker();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setLibrary(player1, List.of(new AcolyteHybrid()));
        int ownHandSize = gd.playerHands.get(player1.getId()).size();
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fountain of Youth");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(ownHandSize + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    private Permanent addAttacker() {
        return addCreatureReady(player1, new AcolyteHybrid());
    }
}
