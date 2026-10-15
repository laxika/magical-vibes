package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TaigamSidisisHand.class, GrizzlyBears.class, Shock.class, Forest.class})
class TaigamSidisisHandTest extends BaseCardTest {

    @Test
    void upkeepPutsOneOfTheTopThreeIntoHandAndTheRestIntoGraveyard() {
        Card first = new Shock();
        Card chosen = new Shock();
        Card third = new Shock();
        harness.setLibrary(player1, List.of(first, chosen, third));
        harness.addToBattlefield(player1, new TaigamSidisisHand());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void controllerSkipsTheirDrawStep() {
        harness.addToBattlefield(player1, new TaigamSidisisHand());
        gd.playerDecks.get(player1.getId()).clear();
        gd.turnNumber = 2;
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    void exiledGraveyardCardsScaleTheTemporaryDebuff() {
        Permanent taigam = addCreatureReady(player1, new TaigamSidisisHand());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setGraveyard(player1, List.of(first, second));
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(taigam.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void activatedAbilityCannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new TaigamSidisisHand());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void skipsTheEntireDrawStepRatherThanOfferingPriorityInIt() {
        harness.addToBattlefield(player1, new TaigamSidisisHand());
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(remaining));
        gd.turnNumber = 2;
        forceMainPhase();
        harness.forceStep(TurnStep.UPKEEP);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.withAutoStop(TurnStep.DRAW, harness::passBothPriorities));

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void upkeepWithOneCardPutsItIntoHandWithoutDrawing() {
        Forest onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addToBattlefield(player1, new TaigamSidisisHand());
        gd.turnNumber = 2;

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void upkeepWithTwoCardsStillRequiresChoosingExactlyOne() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new TaigamSidisisHand());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    void zeroXIsLegalWithAnEmptyGraveyardAndStillTapsTaigam() {
        Permanent taigam = addCreatureReady(player1, new TaigamSidisisHand());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of());
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMultipleCardsChosen(player1, List.of());
        }
        harness.passBothPriorities();

        assertThat(taigam.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void opponentsUpkeepDoesNotTriggerTaigamAndTheirDrawIsUnaffected() {
        harness.addToBattlefield(player1, new TaigamSidisisHand());
        Forest top = new Forest();
        Forest next = new Forest();
        harness.setLibrary(player2, List.of(top, next));
        int handBefore = gd.playerHands.get(player2.getId()).size();
        gd.turnNumber = 2;

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1).contains(top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(next);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void summoningSicknessPreventsActivatingTheTapAbility() {
        harness.addToBattlefield(player1, new TaigamSidisisHand());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest()));
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonlethalDebuffExpiresAtEndOfTurn() {
        addCreatureReady(player1, new TaigamSidisisHand());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Forest payment = new Forest();
        harness.setGraveyard(player1, List.of(payment));
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(payment.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(payment);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
