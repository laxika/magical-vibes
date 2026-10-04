package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EdeaPossessedSorceress.class, GrizzlyBears.class})
class EdeaPossessedSorceressTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, steals, untaps, and hastes an opposing creature")
    void beginningOfCombatStealsUntapsAndHastesOpposingCreature() {
        harness.addToBattlefield(player1, new EdeaPossessedSorceress());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        opposingCreature.tap();

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(opposingCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentController(gd, opposingCreature.getId())).isEqualTo(player1.getId());
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Temporary control and haste expire at end of turn")
    void temporaryControlAndHasteExpireAtEndOfTurn() {
        harness.addToBattlefield(player1, new EdeaPossessedSorceress());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.findPermanentController(gd, opposingCreature.getId())).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Returns a stolen creature to its owner and draws when it dies")
    void returnsStolenCreatureAndDrawsWhenItDies() {
        harness.addToBattlefield(player1, new EdeaPossessedSorceress());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        opposingCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(opposingCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .doesNotContain(opposingCreature.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Does not return or draw for a creature its controller owns")
    void doesNotTriggerForOwnedCreature() {
        harness.addToBattlefield(player1, new EdeaPossessedSorceress());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        ownCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .doesNotContain(ownCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCreature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
