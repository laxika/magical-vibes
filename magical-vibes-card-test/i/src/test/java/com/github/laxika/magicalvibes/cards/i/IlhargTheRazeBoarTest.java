package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KioraBehemothBeckoner;
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

@CardUsed({IlhargTheRazeBoar.class, GrizzlyBears.class, KioraBehemothBeckoner.class})
class IlhargTheRazeBoarTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking may put a creature from hand onto the battlefield tapped and attacking")
    void attackingPutsCreatureTappedAndAttacking() {
        Permanent ilharg = addCreatureReady(player1, new IlhargTheRazeBoar());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.isAttackedThisTurn()).isTrue();
        assertThat(ilharg.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The creature put onto the battlefield returns to its owner's hand at the next end step")
    void attackingCreatureReturnsAtNextEndStep() {
        addCreatureReady(player1, new IlhargTheRazeBoar());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The death trigger may put Ilharg third from the top")
    void deathTriggerPutsIlhargThirdFromTop() {
        Card top = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second, third));
        Permanent ilharg = harness.addToBattlefieldAndReturn(player1, new IlhargTheRazeBoar());
        Card ilhargCard = ilharg.getCard();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ilharg));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), ilhargCard.getId(), third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(ilhargCard.getId()));
    }

    @Test
    @DisplayName("The exile trigger may put Ilharg third from the top")
    void exileTriggerPutsIlhargThirdFromTop() {
        Card top = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second, third));
        Permanent ilharg = harness.addToBattlefieldAndReturn(player1, new IlhargTheRazeBoar());
        Card ilhargCard = ilharg.getCard();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, ilharg));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), ilhargCard.getId(), third.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(ilhargCard.getId()));
    }

    @Test
    void mayDeclinePuttingCreatureOntoBattlefield() {
        addCreatureReady(player1, new IlhargTheRazeBoar());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void mayDeclineReturningFromGraveyardToLibrary() {
        Permanent ilharg = harness.addToBattlefieldAndReturn(player1, new IlhargTheRazeBoar());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ilharg));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Ilharg, the Raze-Boar");
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card.getId().equals(ilharg.getCard().getId()));
    }

    @Test
    void mayDeclineReturningFromExileToLibrary() {
        Permanent ilharg = harness.addToBattlefieldAndReturn(player1, new IlhargTheRazeBoar());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, ilharg));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .contains(ilharg.getCard().getId());
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card.getId().equals(ilharg.getCard().getId()));
    }

    @Test
    void deathTriggerPutsIlhargOnBottomOfShortLibrary() {
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        Permanent ilharg = harness.addToBattlefieldAndReturn(player1, new IlhargTheRazeBoar());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ilharg));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), ilharg.getCard().getId());
    }

    @Test
    void exileTriggerWorksWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent ilharg = harness.addToBattlefieldAndReturn(player1, new IlhargTheRazeBoar());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, ilharg));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(ilharg.getCard().getId());
    }

    @Test
    void creatureThatDiesBeforeEndStepStaysInGraveyard() {
        addCreatureReady(player1, new IlhargTheRazeBoar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    void creatureReturnsEvenAfterIlhargLeavesBattlefield() {
        Permanent ilharg = addCreatureReady(player1, new IlhargTheRazeBoar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ilharg));

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void returnAtEndStepUsesTheStack() {
        Permanent ilharg = addCreatureReady(player1, new IlhargTheRazeBoar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(ilharg.getCard().getId());
        resolveAllTriggers();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void delayedReturnRemainsControlledByIlhargsAbilityController() {
        addCreatureReady(player1, new IlhargTheRazeBoar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.inMutationScope(() -> {
            gd.playerBattlefields.get(player1.getId()).remove(bears);
            gd.playerBattlefields.get(player2.getId()).add(bears);
            gd.stolenCreatures.put(bears.getId(), player1.getId());
            bears.recordControlChange();
            bears.setAttacking(false);
        });

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void oldDeathTriggerCannotMoveIlhargAfterItReturnsAndDiesAgain() {
        Permanent ilharg = harness.addToBattlefieldAndReturn(player1, new IlhargTheRazeBoar());
        Card card = ilharg.getCard();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ilharg));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, card.getId()));
        Permanent returned = harness.enterBattlefieldAndReturn(player1, card);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, returned));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInGraveyard(player1, "Ilharg, the Raze-Boar");
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(c -> c.getId().equals(card.getId()));
    }

    @Test
    void oldExileTriggerCannotMoveIlhargAfterItReturnsAndIsExiledAgain() {
        Permanent ilharg = harness.addToBattlefieldAndReturn(player1, new IlhargTheRazeBoar());
        Card card = ilharg.getCard();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, ilharg));
        harness.inMutationScope(() -> gd.removeFromExile(card.getId()));
        Permanent returned = harness.enterBattlefieldAndReturn(player1, card);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, returned));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId).contains(card.getId());
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(c -> c.getId().equals(card.getId()));
    }

    @Test
    void enteringCreatureOffersItsOwnAttackDestination() {
        addCreatureReady(player1, new IlhargTheRazeBoar());
        harness.enterBattlefieldAndReturn(player2, new KioraBehemothBeckoner());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);

            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player1.getId());
        });
    }
}
