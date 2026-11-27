package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantBeaver;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LaeliaTheBladeReforged;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MakeYourOwnLuck.class, GiantBeaver.class, Forest.class, LaeliaTheBladeReforged.class})
class MakeYourOwnLuckTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and plots a selected nonland card, putting the rest into hand")
    void plotsSelectedNonlandCardAndPutsRestIntoHand() {
        GiantBeaver beaver = new GiantBeaver();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        harness.setHand(player1, List.of(new MakeYourOwnLuck()));
        harness.setLibrary(player1, List.of(firstForest, beaver, secondForest));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(beaver);
        assertThat(gd.plottedCardIds).contains(beaver.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstForest, secondForest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining leaves all looked-at cards in hand")
    void decliningPutsAllCardsIntoHand() {
        GiantBeaver beaver = new GiantBeaver();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        harness.setHand(player1, List.of(new MakeYourOwnLuck()));
        harness.setLibrary(player1, List.of(beaver, firstForest, secondForest));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.plottedCardIds).doesNotContain(beaver.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(beaver, firstForest, secondForest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With no nonland card, puts all looked-at cards into hand without a prompt")
    void noNonlandCardNeedsNoChoice() {
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        Forest thirdForest = new Forest();
        harness.setHand(player1, List.of(new MakeYourOwnLuck()));
        harness.setLibrary(player1, List.of(firstForest, secondForest, thirdForest));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstForest, secondForest, thirdForest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The plotted card is exiled from the library, not from the hand")
    void plottedCardIsExiledFromLibrary() {
        Permanent laelia = addCreatureReady(player1, new LaeliaTheBladeReforged());
        GiantBeaver beaver = new GiantBeaver();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        harness.setHand(player1, List.of(new MakeYourOwnLuck()));
        harness.setLibrary(player1, List.of(firstForest, beaver, secondForest));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.plottedCardIds).contains(beaver.getId());
        assertThat(laelia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An empty library puts nothing anywhere and causes no loss")
    void emptyLibraryDoesNotCauseADrawOrLoss() {
        harness.setHand(player1, List.of(new MakeYourOwnLuck()));
        harness.setLibrary(player1, List.of());
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("With fewer than three cards in the library, still plots from among them")
    void shortLibraryStillAllowsPlotting() {
        GiantBeaver beaver = new GiantBeaver();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new MakeYourOwnLuck()));
        harness.setLibrary(player1, List.of(beaver, forest));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(beaver);
        assertThat(gd.plottedCardIds).contains(beaver.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only a nonland card among the top three can be plotted")
    void onlyTopThreeCardsCanBePlottedAndLandsCannotBeSelected() {
        GiantBeaver existingCard = new GiantBeaver();
        GiantBeaver topCard = new GiantBeaver();
        GiantBeaver fourthCard = new GiantBeaver();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        harness.setHand(player1, List.of(new MakeYourOwnLuck(), existingCard));
        harness.setLibrary(player1, List.of(firstForest, topCard, secondForest, fourthCard));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(topCard);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(existingCard, firstForest, secondForest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourthCard);
    }

    @Test
    @DisplayName("The plotted card can be cast for free only on a later turn at sorcery speed")
    void plottedCardCanBeCastForFreeOnlyOnALaterTurnAtSorcerySpeed() {
        GiantBeaver beaver = new GiantBeaver();
        harness.setHand(player1, List.of(new MakeYourOwnLuck()));
        harness.setLibrary(player1, List.of(beaver));
        addMana();
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, beaver.getId()))
                .isInstanceOf(IllegalStateException.class);
        gd.turnNumber++;
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, beaver.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, beaver.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, beaver.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Beaver");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
