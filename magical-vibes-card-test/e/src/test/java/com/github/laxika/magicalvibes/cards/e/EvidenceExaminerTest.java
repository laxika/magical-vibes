package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvidenceExaminer.class, HillGiant.class})
class EvidenceExaminerTest extends BaseCardTest {

    @Test
    void collectsEvidenceAtBeginningOfCombatAndInvestigates() {
        HillGiant giant = new HillGiant();
        harness.setGraveyard(player1, List.of(giant));
        harness.addToBattlefield(player1, new EvidenceExaminer());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningCollectEvidenceDoesNotInvestigate() {
        HillGiant giant = new HillGiant();
        harness.setGraveyard(player1, List.of(giant));
        harness.addToBattlefield(player1, new EvidenceExaminer());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(giant);
    }

    @Test
    void multipleExiledCardsCauseOnlyOneInvestigation() {
        EvidenceExaminer first = new EvidenceExaminer();
        EvidenceExaminer second = new EvidenceExaminer();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addToBattlefield(player1, new EvidenceExaminer());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
    }

    @Test
    void doesNotCollectEvidenceDuringOpponentsCombat() {
        EvidenceExaminer first = new EvidenceExaminer();
        EvidenceExaminer second = new EvidenceExaminer();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addToBattlefield(player1, new EvidenceExaminer());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void opponentsCollectionDoesNotTriggerInvestigation() {
        EvidenceExaminer first = new EvidenceExaminer();
        EvidenceExaminer second = new EvidenceExaminer();
        harness.setGraveyard(player2, List.of(first, second));
        harness.addToBattlefield(player1, new EvidenceExaminer());
        harness.addToBattlefield(player2, new EvidenceExaminer());

        advanceToCombat(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMultipleCardsChosen(player2, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    void eachExaminerInvestigatesWhenAnotherExaminerCollectsEvidence() {
        EvidenceExaminer first = new EvidenceExaminer();
        EvidenceExaminer second = new EvidenceExaminer();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addToBattlefield(player1, new EvidenceExaminer());
        harness.addToBattlefield(player1, new EvidenceExaminer());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void insufficientGraveyardManaValueDoesNotCollectEvidence() {
        EvidenceExaminer evidence = new EvidenceExaminer();
        harness.setGraveyard(player1, List.of(evidence));
        harness.addToBattlefield(player1, new EvidenceExaminer());

        advanceToCombat(player1);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence);
        assertThat(gd.findExiledCard(evidence.getId())).isNull();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
