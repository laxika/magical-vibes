package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilasRennSeekerAdept.class, Ornithopter.class, Forest.class})
class SilasRennSeekerAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage targets an artifact card in the controller's graveyard")
    void targetsOwnArtifactCard() {
        Card artifact = new Ornithopter();
        Card nonArtifact = new Forest();
        Card opponentArtifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact, nonArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));

        dealCombatDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
    }

    @Test
    @DisplayName("Can cast the targeted artifact from the graveyard this turn")
    void castsTargetedArtifactFromGraveyard() {
        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));

        dealCombatDamage();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();
        harness.castFromGraveyard(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("The trigger does not fire without an artifact card in the controller's graveyard")
    void doesNotTriggerWithoutLegalArtifact() {
        harness.setGraveyard(player1, List.of(new Forest()));

        dealCombatDamage();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void dealCombatDamage() {
        Permanent silas = addCreatureReady(player1, new SilasRennSeekerAdept());
        silas.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
