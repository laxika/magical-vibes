package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Transpose.class, GrizzlyBears.class, Island.class, Shock.class})
class TransposeTest extends BaseCardTest {

    @Test
    void drawsDiscardsLosesLifeAndCreatesWizardWhenCastFromHand() {
        Island drawnCard = new Island();
        GrizzlyBears discardedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Transpose(), discardedCard));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedCard);
        harness.assertLife(player1, lifeBefore - 1);
        assertThat(countPermanents(player1, "Wizard")).isEqualTo(1);
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void wizardDealsDamageWhenControllerCastsNoncreatureSpell() {
        Transpose transpose = new Transpose();
        GrizzlyBears discardedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(transpose, discardedCard, new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    @Test
    void reboundDoesNotCreateAnotherWizardWhenCastFromExile() {
        Transpose transpose = new Transpose();
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(transpose, new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(countPermanents(player1, "Wizard")).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Wizard")).isEqualTo(1);
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void canDiscardTheCardJustDrawn() {
        Island drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Transpose(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Wizard")).isEqualTo(1);
    }

    @Test
    void wizardDoesNotTriggerForCreatureSpell() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new Transpose(), new Island(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    void wizardDoesNotTriggerForOpponentsNoncreatureSpell() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new Transpose(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void reboundRepeatsLootAndLifeLossAndTriggersExistingWizard() {
        Transpose transpose = new Transpose();
        Island firstDraw = new Island();
        Island secondDraw = new Island();
        GrizzlyBears firstDiscard = new GrizzlyBears();
        GrizzlyBears secondDiscard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(transpose, firstDiscard, secondDiscard));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.findExiledCard(transpose.getId())).isNotNull();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(firstDiscard, secondDiscard, transpose);
        assertThat(gd.findExiledCard(transpose.getId())).isNull();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
        assertThat(countPermanents(player1, "Wizard")).isEqualTo(1);
    }

    @Test
    void decliningReboundLeavesCardExiledAndDoesNotOfferItAgain() {
        Transpose transpose = new Transpose();
        Island remainingCard = new Island();
        harness.setLibrary(player1, List.of(new Island(), remainingCard));
        harness.setHand(player1, List.of(transpose, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(transpose.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Wizard")).isEqualTo(1);
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.findExiledCard(transpose.getId())).isNotNull();
    }
}
