package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValakutExploration.class, Forest.class, CanopyBaloth.class})
class ValakutExplorationTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall exiles the top card of the controller's library")
    void landfallExilesTopCard() {
        Permanent exploration = addExploration();
        Card topCard = new CanopyBaloth();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(exploration.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("At the controller's end step, remaining exiled cards go to their owners' graveyards and damage each opponent")
    void endStepReturnsCardsAndDealsDamage() {
        Permanent exploration = addExploration();
        Card topCard = new CanopyBaloth();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(exploration.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("The play permission remains after Valakut Exploration leaves the battlefield")
    void playPermissionRemainsAfterSourceLeaves() {
        Permanent exploration = addExploration();
        Card exiledCard = new CanopyBaloth();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(exploration);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Canopy Baloth");
    }

    @Test
    @DisplayName("The end-step ability does not trigger without cards exiled with Valakut Exploration")
    void endStepDoesNotTriggerWithoutExiledCards() {
        addExploration();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Landfall with an empty library does not exile a card or cause a loss")
    void emptyLibraryDoesNotExile() {
        Permanent exploration = addExploration();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(exploration.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Valakut Exploration");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        addExploration();
        Card topCard = new CanopyBaloth();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An exiled land can be played when a land play is available and triggers landfall again")
    void canPlayExiledLand() {
        Permanent exploration = addExploration();
        Card exiledLand = new Forest();
        Card nextCard = new CanopyBaloth();
        harness.setLibrary(player1, List.of(exiledLand, nextCard));
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        harness.castFromExile(player1, exiledLand.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(exiledLand.getId()));
        assertThat(gd.getCardsExiledByPermanent(exploration.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("Casting an exiled card removes it from the end-step damage count")
    void playedCardDoesNotDealEndStepDamage() {
        Permanent exploration = addExploration();
        Card exiledCard = new CanopyBaloth();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.getCardsExiledByPermanent(exploration.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player1, "Canopy Baloth");
    }

    @Test
    @DisplayName("A landfall ability still grants play permission if its source leaves before resolution")
    void landfallResolvesAfterSourceLeaves() {
        Permanent exploration = addExploration();
        Card exiledCard = new CanopyBaloth();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, exploration));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Canopy Baloth");
    }

    @Test
    @DisplayName("Each copy only moves and counts the cards exiled with that copy")
    void separateCopiesTrackTheirOwnCards() {
        Permanent first = addExploration();
        Permanent second = addExploration();
        Card firstCard = new CanopyBaloth();
        Card secondCard = new CanopyBaloth();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getCardsExiledByPermanent(first.getId())).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(second.getId())).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(first.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(second.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCard, secondCard);
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An end-step ability already on the stack resolves after its source leaves")
    void endStepResolvesAfterSourceLeaves() {
        Permanent exploration = addExploration();
        Card exiledCard = new CanopyBaloth();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, exploration));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(exiledCard);
        assertThat(gd.getCardsExiledByPermanent(exploration.getId())).isEmpty();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The play permission does not allow an extra land play")
    void exiledLandStillNeedsAnAvailableLandPlay() {
        Permanent exploration = addExploration();
        Card exiledLand = new Forest();
        harness.setLibrary(player1, List.of(exiledLand));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getCardsExiledByPermanent(exploration.getId())).containsExactly(exiledLand);
    }

    @Test
    @DisplayName("The opponent's end step does not move exiled cards or deal damage")
    void opponentEndStepDoesNotTrigger() {
        Permanent exploration = addExploration();
        Card exiledCard = new CanopyBaloth();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(exploration.getId())).containsExactly(exiledCard);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The end-step ability counts all cards still exiled with one copy")
    void multipleLandfallsDealDamageForEveryRemainingCard() {
        Permanent exploration = addExploration();
        Card firstCard = new CanopyBaloth();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        assertThat(gd.getCardsExiledByPermanent(exploration.getId())).containsExactly(firstCard, secondCard);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(exploration.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCard, secondCard);
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }
    private Permanent addExploration() {
        return harness.addToBattlefieldAndReturn(player1, new ValakutExploration());
    }

}
