package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GuadosalamFarplaneGateway;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TownGreeter.class, Forest.class, GuadosalamFarplaneGateway.class})
class TownGreeterTest extends BaseCardTest {

    @Test
    void putsMilledTownIntoHandAndGainsLife() {
        Card town = new GuadosalamFarplaneGateway();
        harness.setLibrary(player1, List.of(town, new TownGreeter(), new TownGreeter(), new TownGreeter()));

        castTownGreeter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(town);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void putsMilledNonTownLandIntoHandWithoutGainingLife() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new TownGreeter(), new TownGreeter(), new TownGreeter()));

        castTownGreeter();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void decliningTheTownDoesNotGainLifeOrReturnIt() {
        Card town = new GuadosalamFarplaneGateway();
        harness.setLibrary(player1, List.of(town, new TownGreeter(), new TownGreeter(), new TownGreeter()));

        castTownGreeter();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(town);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(town);
    }

    @Test
    void canDeclineFirstLandAndTakeLaterTown() {
        Forest forest = new Forest();
        Card town = new GuadosalamFarplaneGateway();
        harness.setLibrary(player1, List.of(forest, town, new TownGreeter(), new TownGreeter()));

        castTownGreeter();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(town);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest).hasSize(3);
        harness.assertLife(player1, 22);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void takingFirstLandPreventsReturningAnotherOrGainingTownBonus() {
        Forest forest = new Forest();
        Card town = new GuadosalamFarplaneGateway();
        harness.setLibrary(player1, List.of(forest, town, new TownGreeter(), new TownGreeter()));

        castTownGreeter();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(town).hasSize(3);
        harness.assertLife(player1, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void millsOnlyTopFourAndCannotReturnAnOlderGraveyardLand() {
        Forest oldLand = new Forest();
        Forest fifthCard = new Forest();
        List<Card> milled = List.of(new TownGreeter(), new TownGreeter(), new TownGreeter(), new TownGreeter());
        harness.setGraveyard(player1, List.of(oldLand));
        harness.setLibrary(player1, List.of(milled.get(0), milled.get(1), milled.get(2), milled.get(3), fifthCard));

        castTownGreeter();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(milled).contains(oldLand).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifthCard);
        harness.assertLife(player1, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void millsAvailableCardsFromShortLibraryAndReturnsTown() {
        Card town = new GuadosalamFarplaneGateway();
        Card creature = new TownGreeter();
        harness.setLibrary(player1, List.of(town, creature));

        castTownGreeter();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(town);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        harness.assertLife(player1, 22);
    }

    private void castTownGreeter() {
        harness.castFromHand(player1, new TownGreeter(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
