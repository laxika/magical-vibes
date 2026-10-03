package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.i.InfernalGrasp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilasRennSeekerAdept.class, AetherSpellbomb.class, InfernalGrasp.class})
class SilasRennSeekerAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage lets its controller cast a chosen artifact from the graveyard")
    void combatDamageGrantsCastPermissionForChosenArtifact() {
        Card artifact = new AetherSpellbomb();
        Card nonArtifact = new InfernalGrasp();
        harness.setGraveyard(player1, List.of(artifact, nonArtifact));
        Permanent silas = addCreatureReady(player1, new SilasRennSeekerAdept());
        silas.setAttacking(true);

        resolveCombat();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aether Spellbomb");
        harness.assertInGraveyard(player1, "Infernal Grasp");
    }

    @Test
    @DisplayName("No trigger is created when the controller's graveyard has no artifact")
    void noTriggerWithoutArtifactCard() {
        Card nonArtifact = new InfernalGrasp();
        harness.setGraveyard(player1, List.of(nonArtifact));
        Permanent silas = addCreatureReady(player1, new SilasRennSeekerAdept());
        silas.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Infernal Grasp");
    }
}
