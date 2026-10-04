package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.Aquamoeba;
import com.github.laxika.magicalvibes.cards.c.CephalidAristocrat;
import com.github.laxika.magicalvibes.cards.d.DawnOfTheDead;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hypnox.class, Aquamoeba.class, CephalidAristocrat.class, DawnOfTheDead.class})
class HypnoxTest extends BaseCardTest {

    @Test
    @DisplayName("When cast from hand, Hypnox exiles all cards from a target opponent's hand")
    void castFromHandExilesTargetOpponentsHand() {
        Card first = new Aquamoeba();
        Card second = new CephalidAristocrat();
        castHypnoxWithTargetHand(List.of(first, second));

        Permanent hypnox = findPermanent(player1, "Hypnox");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .filteredOn(ExiledCardEntry::sourcePermanentId, hypnox.getId())
                .extracting(ExiledCardEntry::card)
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("When Hypnox leaves the battlefield, its exiled cards return to their owners' hands")
    void exiledCardsReturnWhenHypnoxLeaves() {
        Card first = new Aquamoeba();
        Card second = new CephalidAristocrat();
        castHypnoxWithTargetHand(List.of(first, second));
        Permanent hypnox = findPermanent(player1, "Hypnox");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hypnox));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.exiledCards)
                .noneMatch(entry -> hypnox.getId().equals(entry.sourcePermanentId()));
    }

    @Test
    @DisplayName("Entering from the graveyard does not trigger Hypnox's hand exile")
    void enteringFromGraveyardDoesNotExileHand() {
        Card handCard = new Aquamoeba();
        Card hypnox = new Hypnox();
        harness.setHand(player2, new ArrayList<>(List.of(handCard)));
        harness.setGraveyard(player1, List.of(hypnox));
        harness.addToBattlefield(player1, new DawnOfTheDead());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(hypnox.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hypnox");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card() == handCard);
    }

    @Test
    @DisplayName("Hypnox's ETB trigger can target only an opponent")
    void etbTriggerTargetsOnlyOpponent() {
        harness.setHand(player2, new ArrayList<>(List.of(new Aquamoeba())));
        harness.castFromHand(player1, new Hypnox(), "{8}{B}{B}{B}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering the battlefield without being cast from hand does not exile an opponent's hand")
    void enteringWithoutBeingCastDoesNotExileHand() {
        Card handCard = new Aquamoeba();
        harness.setHand(player2, new ArrayList<>(List.of(handCard)));

        harness.enterBattlefieldAndReturn(player1, new Hypnox());

        harness.assertOnBattlefield(player1, "Hypnox");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card() == handCard);
    }

    @Test
    @DisplayName("An empty opposing hand is a legal target and exiles nothing")
    void emptyHandIsLegalTarget() {
        castHypnoxWithTargetHand(List.of());

        harness.assertOnBattlefield(player1, "Hypnox");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Hypnox returns only the cards exiled by its own ability")
    void separateHypnoxInstancesKeepTheirExiledCardsSeparate() {
        Card firstCard = new Aquamoeba();
        castHypnoxWithTargetHand(List.of(firstCard));
        Permanent firstHypnox = findPermanent(player1, "Hypnox");

        Card secondCard = new CephalidAristocrat();
        castHypnoxWithTargetHand(List.of(secondCard));
        Permanent secondHypnox = findPermanents(player1, "Hypnox").stream()
                .filter(permanent -> !permanent.getId().equals(firstHypnox.getId()))
                .findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, firstHypnox));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstCard);
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(secondCard);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, secondHypnox));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(firstCard, secondCard);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Leaving before the enter trigger resolves leaves the subsequently exiled hand in exile")
    void leavingBeforeExileTriggerResolvesDoesNotPreventExile() {
        Card handCard = new Aquamoeba();
        harness.setHand(player2, List.of(handCard));
        harness.castFromHand(player1, new Hypnox(), "{8}{B}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent hypnox = findPermanent(player1, "Hypnox");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hypnox));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.exiledCards).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(handCard);
        assertThat(gd.stack).isEmpty();
    }

    private void castHypnoxWithTargetHand(List<Card> targetHand) {
        harness.setHand(player2, new ArrayList<>(targetHand));
        harness.castFromHand(player1, new Hypnox(), "{8}{B}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
