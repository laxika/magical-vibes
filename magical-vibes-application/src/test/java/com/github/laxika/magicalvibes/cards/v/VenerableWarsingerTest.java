package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VenerableWarsingerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage returns a chosen creature card with mana value up to the damage dealt")
    void combatDamageReturnsCreatureWithinDamageLimit() {
        Card legal = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(legal, tooExpensive));

        attackWithWarsingerDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(legal.getId());

        harness.handleMultipleCardsChosen(player1, List.of(legal.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(legal.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(tooExpensive);
    }

    @Test
    @DisplayName("Combat damage does not offer creatures above the damage limit")
    void noLegalCreatureDoesNotPrompt() {
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(tooExpensive));

        attackWithWarsingerDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(tooExpensive);
    }

    @Test
    @DisplayName("The optional return may be declined")
    void returnMayBeDeclined() {
        Card legal = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(legal));

        attackWithWarsingerDealingDamage();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(legal);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(legal.getId()));
    }

    private void attackWithWarsingerDealingDamage() {
        Permanent warsinger = addCreatureReady(player1, new VenerableWarsinger());
        warsinger.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
