package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gurzigost.class, GrizzlyBears.class, HillGiant.class})
class GurzigostTest extends BaseCardTest {

    @Test
    @DisplayName("Gurzigost can put two cards from its controller's graveyard on the library bottom")
    void paysUpkeepWithTwoCardsFromOwnGraveyard() {
        Permanent gurzigost = addCreatureReady(player1, new Gurzigost());
        Card firstGraveyardCard = new GrizzlyBears();
        Card secondGraveyardCard = new HillGiant();
        Card libraryCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstGraveyardCard, secondGraveyardCard));
        harness.setLibrary(player1, new ArrayList<>(List.of(libraryCard)));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstGraveyardCard.getId(), secondGraveyardCard.getId());
        harness.handleMultipleCardsChosen(player1,
                List.of(firstGraveyardCard.getId(), secondGraveyardCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gurzigost);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(libraryCard, firstGraveyardCard, secondGraveyardCard);
    }

    @Test
    @DisplayName("Gurzigost is sacrificed when its controller cannot put two cards on the library bottom")
    void insufficientOwnGraveyardCardsSacrificesIt() {
        Permanent gurzigost = addCreatureReady(player1, new Gurzigost());
        Card ownGraveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownGraveyardCard));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gurzigost);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownGraveyardCard, gurzigost.getCard());
    }

    @Test
    @DisplayName("Gurzigost may assign its combat damage as though it were unblocked")
    void mayAssignCombatDamageAsThoughUnblocked() {
        Permanent gurzigost = addCreatureReady(player1, new Gurzigost());
        Card discardCard = new HillGiant();
        harness.setHand(player1, List.of(discardCard));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        gurzigost.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 6));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }
}
