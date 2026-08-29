package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BatterhornTest extends BaseCardTest {

    private void castAndAcceptMay(UUID artifactId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Batterhorn()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifactId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("ETB destroys the chosen target artifact")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID artifactId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndAcceptMay(artifactId);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Batterhorn");
    }

    @Test
    @DisplayName("Declining the may ability leaves the artifact alone")
    void decliningMaySkipsDestruction() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Batterhorn()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Leonin Scimitar"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Batterhorn");
    }

    @Test
    @DisplayName("No may prompt when no artifact is on the battlefield")
    void noMayPromptWithoutArtifact() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Batterhorn()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Batterhorn");
    }

    @Test
    @DisplayName("Resolving the targeted ETB prompts for the may choice")
    void resolvingTargetedEtbPromptsForMay() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Batterhorn()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Leonin Scimitar"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Can target an artifact you control")
    void canTargetOwnArtifact() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        UUID artifactId = harness.getPermanentId(player1, "Leonin Scimitar");
        castAndAcceptMay(artifactId);

        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }
}
