package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
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

@CardUsed({HarnessedSnubhorn.class, TormodsCrypt.class, Pacifism.class, GrizzlyBears.class})
class HarnessedSnubhornTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage targets an artifact or enchantment card and returns it to the battlefield")
    void returnsTargetArtifactOrEnchantment() {
        Card artifact = new TormodsCrypt();
        Card enchantment = new Pacifism();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, enchantment, creature));

        dealCombatDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId(), enchantment.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(enchantment, creature);
    }

    @Test
    @DisplayName("The combat-damage trigger is not created without a matching graveyard card")
    void noMatchingGraveyardCardProducesNoTrigger() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        dealCombatDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    private void dealCombatDamage() {
        Permanent snubhorn = addCreatureReady(player1, new HarnessedSnubhorn());
        snubhorn.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
