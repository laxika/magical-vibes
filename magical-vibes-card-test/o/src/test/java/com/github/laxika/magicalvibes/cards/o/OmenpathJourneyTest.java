package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({OmenpathJourney.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class OmenpathJourneyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB searches for up to five lands with different names and tracks them")
    void etbSearchesForDifferentNamedLands() {
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        Island island = new Island();
        Mountain mountain = new Mountain();
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        OmenpathJourney card = new OmenpathJourney();
        harness.setLibrary(player1, List.of(firstForest, secondForest, island, mountain, plains, swamp));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getId)
                .containsExactlyInAnyOrder(firstForest.getId(), secondForest.getId(), island.getId(),
                        mountain.getId(), plains.getId(), swamp.getId());

        chooseCard(firstForest);
        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getId).doesNotContain(secondForest.getId());

        chooseCard(island);
        chooseCard(mountain);
        chooseCard(plains);
        chooseCard(swamp);

        Permanent journey = findPermanent(player1, "Omenpath Journey");
        assertThat(gd.getCardsExiledByPermanent(journey.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(firstForest.getId(), island.getId(), mountain.getId(),
                        plains.getId(), swamp.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(secondForest.getId());
    }

    @Test
    @DisplayName("Your end step returns one tracked land at random tapped")
    void endStepReturnsOneTrackedLandTapped() {
        OmenpathJourney card = new OmenpathJourney();
        Permanent journey = harness.addToBattlefieldAndReturn(player1, card);
        Forest forest = new Forest();
        Island island = new Island();
        gd.addToExile(player1.getId(), forest, journey.getId());
        gd.addToExile(player1.getId(), island, journey.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(forest.getId())
                        || permanent.getCard().getId().equals(island.getId()))
                .toList();
        assertThat(returned).hasSize(1);
        assertThat(returned.getFirst().isTapped()).isTrue();
        assertThat(gd.getCardsExiledByPermanent(journey.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The search may exile no lands even when lands are available")
    void mayDeclineEntireSearch() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new OmenpathJourney()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Omenpath Journey").getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The search may stop after one land and excludes nonlands")
    void mayStopAfterOneLand() {
        Forest forest = new Forest();
        Island island = new Island();
        OmenpathJourney nonland = new OmenpathJourney();
        harness.setLibrary(player1, List.of(forest, island, nonland));
        harness.setHand(player1, List.of(new OmenpathJourney()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, island);
        chooseCard(forest);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Omenpath Journey").getId()))
                .containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(island, nonland);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's end step does not return a land")
    void opponentEndStepDoesNotReturnLand() {
        Permanent journey = harness.addToBattlefieldAndReturn(player1, new OmenpathJourney());
        Forest forest = new Forest();
        gd.addToExile(player1.getId(), forest, journey.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(journey.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("A journey cannot return cards exiled with another journey or unlinked cards")
    void endStepUsesOnlyItsOwnExiledCards() {
        Permanent journey = harness.addToBattlefieldAndReturn(player1, new OmenpathJourney());
        Permanent otherJourney = harness.addToBattlefieldAndReturn(player2, new OmenpathJourney());
        Forest forest = new Forest();
        Island island = new Island();
        Mountain unlinked = new Mountain();
        gd.addToExile(player1.getId(), forest, journey.getId());
        gd.addToExile(player2.getId(), island, otherJourney.getId());
        gd.addToExile(player1.getId(), unlinked);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.getCardsExiledByPermanent(journey.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(otherJourney.getId())).containsExactly(island);
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.findExiledCard(unlinked.getId())).isNotNull();
    }

    @Test
    @DisplayName("The end-step ability does nothing when no cards remain exiled with it")
    void emptyExileDoesNothing() {
        harness.addToBattlefield(player1, new OmenpathJourney());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void chooseCard(Card card) {
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int index = search.params().cards().stream()
                .map(Card::getId)
                .toList()
                .indexOf(card.getId());
        assertThat(index).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
