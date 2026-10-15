package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FireDiamond;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SalvageScuttler.class, FireDiamond.class, GrizzlyBears.class})
class SalvageScuttlerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking prompts its controller to return an artifact they control")
    void attackingPromptsArtifactBounce() {
        addCreatureReady(player1, new SalvageScuttler());
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, new FireDiamond()).getId();
        UUID opponentArtifactId = harness.addToBattlefieldAndReturn(player2, new FireDiamond()).getId();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(artifactId)
                .doesNotContain(opponentArtifactId);
    }

    @Test
    @DisplayName("The chosen artifact is returned to its owner's hand")
    void chosenArtifactReturnsToHand() {
        addCreatureReady(player1, new SalvageScuttler());
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, new FireDiamond()).getId();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, artifactId);

        harness.assertNotOnBattlefield(player1, "Fire Diamond");
        harness.assertInHand(player1, "Fire Diamond");
        harness.assertOnBattlefield(player1, "Salvage Scuttler");
    }

    @Test
    @DisplayName("Non-artifacts and a lack of controlled artifacts do not create a choice")
    void noControlledArtifactMeansNoChoice() {
        addCreatureReady(player1, new SalvageScuttler());
        harness.addToBattlefield(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Salvage Scuttler");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A controlled artifact owned by the opponent returns to the opponent's hand")
    void controlledArtifactReturnsToItsActualOwner() {
        addCreatureReady(player1, new SalvageScuttler());
        FireDiamond artifact = new FireDiamond();
        artifact.setOwnerId(player2.getId());
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, artifact).getId();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, artifactId);

        harness.assertNotOnBattlefield(player1, "Fire Diamond");
        harness.assertInHand(player2, "Fire Diamond");
        harness.assertNotInHand(player1, "Fire Diamond");
    }

    @Test
    @DisplayName("The controller chooses exactly one artifact when several are available")
    void returnsOnlyTheChosenArtifact() {
        addCreatureReady(player1, new SalvageScuttler());
        UUID firstId = harness.addToBattlefieldAndReturn(player1, new FireDiamond()).getId();
        UUID secondId = harness.addToBattlefieldAndReturn(player1, new FireDiamond()).getId();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(firstId, secondId);
        harness.handlePermanentChosen(player1, secondId);

        assertThat(findPermanents(player1, "Fire Diamond"))
                .extracting(permanent -> permanent.getId()).containsExactly(firstId);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Fire Diamond")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Another creature attacking does not trigger Salvage Scuttler")
    void anotherCreatureAttackingDoesNotReturnAnArtifact() {
        addCreatureReady(player1, new SalvageScuttler());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FireDiamond());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Fire Diamond");
        harness.assertNotInHand(player1, "Fire Diamond");
    }
}
