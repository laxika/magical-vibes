package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HighwayRobbery;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlickshotLockpicker.class, Shock.class, GrizzlyBears.class, HighwayRobbery.class, ThinkTwice.class})
class SlickshotLockpickerTest extends BaseCardTest {

    @Test
    void entersAndOnlyTargetsInstantOrSorceryInControllerGraveyard() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, bears));
        castLockpicker();

        List<java.util.UUID> validIds = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds();

        assertThat(validIds).containsExactly(shock.getId());
    }

    @Test
    void grantsFlashbackAndExilesTheCastCard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castLockpicker();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveFlashback(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    void doesNotPromptWithoutAValidGraveyardTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castLockpicker();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    void canBePlottedForItsPlotCost() {
        SlickshotLockpicker lockpicker = new SlickshotLockpicker();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(lockpicker));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(lockpicker);
        assertThat(gd.plottedCardIds).contains(lockpicker.getId());
    }

    @Test
    void includesSorceriesButNotOpponentsGraveyardCards() {
        HighwayRobbery robbery = new HighwayRobbery();
        Shock opposingShock = new Shock();
        harness.setGraveyard(player1, List.of(robbery));
        harness.setGraveyard(player2, List.of(opposingShock));

        castLockpicker();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(robbery.getId());
    }

    @Test
    void onlyTheSelectedCardGainsFlashback() {
        Shock selected = new Shock();
        Shock unselected = new Shock();
        harness.setGraveyard(player1, List.of(selected, unselected));
        castLockpicker();
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(selected, unselected);
        harness.castAndResolveFlashback(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
    }

    @Test
    void grantedFlashbackExpiresAtEndOfTurn() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock()));
        castLockpicker();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    void canUseGrantedCostEvenWhenTheTargetAlreadyHasFlashback() {
        ThinkTwice thinkTwice = new ThinkTwice();
        Shock drawnCard = new Shock();
        harness.setGraveyard(player1, List.of(thinkTwice));
        harness.setLibrary(player1, List.of(drawnCard));
        castLockpicker();
        harness.handleMultipleCardsChosen(player1, List.of(thinkTwice.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatCode(() -> harness.castFlashback(player1, 0)).doesNotThrowAnyException();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(thinkTwice);
    }

    @Test
    void plottedCardCannotBeCastOnTheSameTurn() {
        SlickshotLockpicker lockpicker = plotLockpicker();

        assertThatThrownBy(() -> harness.castFromExile(player1, lockpicker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(lockpicker);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plottedCardCanBeCastForFreeOnALaterTurnAndStillGrantsFlashback() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock()));
        SlickshotLockpicker lockpicker = plotLockpicker();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        harness.castFromExile(player1, lockpicker.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertOnBattlefield(player1, "Slickshot Lockpicker");
        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock).doesNotContain(lockpicker);
    }

    @Test
    void plottingIsRestrictedToSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        SlickshotLockpicker lockpicker = new SlickshotLockpicker();
        harness.setHand(player1, List.of(lockpicker));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).contains(lockpicker);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(lockpicker);
    }

    private SlickshotLockpicker plotLockpicker() {
        SlickshotLockpicker lockpicker = new SlickshotLockpicker();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(lockpicker));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castWithAlternateCost(player1, 0, List.of());
        return lockpicker;
    }

    private void castLockpicker() {
        harness.castFromHand(player1, new SlickshotLockpicker(), "{2}{U}");
        harness.passBothPriorities();
    }
}
