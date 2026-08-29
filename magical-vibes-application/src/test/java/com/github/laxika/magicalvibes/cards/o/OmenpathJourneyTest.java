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
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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

        Permanent journey = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
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

    private void chooseCard(Card card) {
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int index = search.params().cards().stream()
                .map(Card::getId)
                .toList()
                .indexOf(card.getId());
        assertThat(index).isGreaterThanOrEqualTo(0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
