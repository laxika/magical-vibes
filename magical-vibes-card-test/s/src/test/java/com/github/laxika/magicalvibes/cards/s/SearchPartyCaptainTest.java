package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GavonyTrapper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SearchPartyCaptain.class, GavonyTrapper.class})
class SearchPartyCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each creature attacked with this turn")
    void costIsReducedByCreaturesAttackedWithThisTurn() {
        addCreatureReady(player1, new GavonyTrapper());
        addCreatureReady(player1, new GavonyTrapper());
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SearchPartyCaptain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot cast for less mana when no creature was attacked with this turn")
    void fullCostIsRequiredWithoutAttacking() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SearchPartyCaptain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enters the battlefield and draws a card")
    void entersAndDrawsACard() {
        harness.setHand(player1, List.of(new SearchPartyCaptain()));
        harness.setLibrary(player1, List.of(new GavonyTrapper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Four attackers reduce the generic cost to zero")
    void reductionCannotMakeTheGenericCostNegative() {
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new GavonyTrapper());
        }
        declareAttackers(List.of(0, 1, 2, 3));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new SearchPartyCaptain()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Attacking does not reduce the white mana requirement")
    void reductionDoesNotPayTheColoredCost() {
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new GavonyTrapper());
        }
        declareAttackers(List.of(0, 1, 2, 3));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new SearchPartyCaptain()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The same creature attacking in two combats counts only once")
    void repeatedAttackerCountsOnlyOnce() {
        Permanent attacker = addCreatureReady(player1, new GavonyTrapper());
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> declareAttackers(List.of(0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        attacker.untap();
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> declareAttackers(List.of(0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new SearchPartyCaptain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attackers still count after leaving the battlefield")
    void departedAttackersStillReduceCost() {
        Permanent first = addCreatureReady(player1, new GavonyTrapper());
        Permanent second = addCreatureReady(player1, new GavonyTrapper());
        declareAttackers(List.of(0, 1));
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
        });
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new SearchPartyCaptain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's attackers do not reduce the cost")
    void opponentAttackersDoNotReduceCost() {
        addCreatureReady(player2, new GavonyTrapper());
        addCreatureReady(player2, new GavonyTrapper());
        declareAttackers(player2, List.of(0, 1));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new SearchPartyCaptain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attackers from an earlier turn do not reduce the cost")
    void costReductionExpiresAtTheEndOfTheTurn() {
        addCreatureReady(player1, new GavonyTrapper());
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> declareAttackers(List.of(0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SearchPartyCaptain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The draw trigger resolves even if the captain leaves the battlefield")
    void drawTriggerSurvivesSourceRemoval() {
        harness.setHand(player1, List.of(new SearchPartyCaptain()));
        harness.setLibrary(player1, List.of(new GavonyTrapper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Search Party Captain");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        Permanent captain = findPermanent(player1, "Search Party Captain");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, captain));

        resolveAllTriggers();

        harness.assertInHand(player1, "Gavony Trapper");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
