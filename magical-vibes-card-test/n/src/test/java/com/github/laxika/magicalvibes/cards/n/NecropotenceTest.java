package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.l.LibraryOfLeng;
import com.github.laxika.magicalvibes.cards.m.MindRavel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Necropotence.class, Disenchant.class, MindRavel.class, LibraryOfLeng.class})
class NecropotenceTest extends BaseCardTest {

    @Test
    @DisplayName("Controller skips their draw step")
    void controllerSkipsDrawStep() {
        harness.setLibrary(player1, List.of(new Disenchant()));
        harness.addToBattlefield(player1, new Necropotence());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Pay 1 life exiles the top card face down and returns it at the controller's end step")
    void payOneLifeExilesTopCardAndReturnsAtEndStep() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);

        Card top = new Disenchant();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // 1 life paid, card exiled face down (not yet in hand), off the top of the library
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.findExiledCard(top.getId()).faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(c -> c.getId().equals(top.getId()));

        // Advance to the controller's end step → the set-aside card returns to hand
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(c -> c.getId().equals(top.getId()));
    }

    @Test
    @DisplayName("Set-aside card returns only at the controller's own end step, not an opponent's")
    void setAsideCardReturnsOnlyAtOwnEndStep() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);

        Card top = new Disenchant();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // The opponent's end step must NOT return the card
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(top.getId()));

        // The controller's next end step returns it
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(top.getId()));
    }

    @Test
    @DisplayName("Two activations set aside two cards; both return at the end step")
    void multipleActivationsAllReturn() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);

        Card first = new Disenchant();
        Card second = new Disenchant();
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(first.getId()))
                .anyMatch(c -> c.getId().equals(second.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(first.getId()))
                .anyMatch(c -> c.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Empty library: paying 1 life exiles nothing")
    void emptyLibraryExilesNothing() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Delayed return still occurs if Necropotence leaves the battlefield")
    void delayedReturnSurvivesSourceLeavingBattlefield() {
        Permanent necropotence = harness.addToBattlefieldAndReturn(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);

        Card top = new Disenchant();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, necropotence.getId());

        harness.assertNotOnBattlefield(player1, "Necropotence");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(c -> c.getId().equals(top.getId()));
    }

    @Test
    @DisplayName("Delayed return waits on the stack for priority before returning the card")
    void delayedReturnWaitsForPriority() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);

        Card top = new Disenchant();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getId().equals(top.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(c -> c.getId().equals(top.getId()));
    }

    @Test
    @DisplayName("A discarded card is exiled from the controller's graveyard, not left there")
    void discardedCardIsExiledFromGraveyard() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new MindRavel(), new Disenchant()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        Card toDiscard = gd.playerHands.get(player1.getId()).get(0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        // The discarded card is exiled (Necropotence's trigger), not left in the graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(toDiscard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(toDiscard.getId()));
    }

    @Test
    @DisplayName("A discarded card remains in the graveyard until Necropotence's trigger resolves")
    void discardedCardWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new MindRavel(), new Disenchant()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        Card toDiscard = gd.playerHands.get(player1.getId()).get(0);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handleCardChosen(player1, 0));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(toDiscard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(toDiscard.getId()));

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(toDiscard.getId()));
    }

    @Test
    @DisplayName("An opponent's discard does not trigger Necropotence")
    void opponentDiscardDoesNotTriggerNecropotence() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new MindRavel(), new Disenchant()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player2, 0, player2.getId());

        Card toDiscard = gd.playerHands.get(player2.getId()).get(0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(toDiscard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(toDiscard.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getId().equals(toDiscard.getId()));
    }

    @Test
    @DisplayName("Skipping the draw step advances directly from upkeep to the main phase")
    void skipsEntireDrawStepIncludingPriorityWindow() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Discard still triggers when Library of Leng puts the card on top of the library")
    void discardReplacementDoesNotSuppressTrigger() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card discarded = new Disenchant();
        harness.setHand(player1, List.of(new MindRavel(), discarded));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(discarded);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof Necropotence);
        resolveAllTriggers();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(discarded.getId()));
    }

    @Test
    @DisplayName("Activation during an end step waits until the controller's following end step")
    void activationAfterEndStepBeginsWaitsForFollowingEndStep() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        Card top = new Disenchant();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("Returning a face-down card to hand does not reveal it in the public log")
    void delayedReturnDoesNotRevealCardIdentity() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card top = new Disenchant();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gameLogContains("Disenchant")).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gameLogContains("Disenchant")).isFalse();
    }
}
