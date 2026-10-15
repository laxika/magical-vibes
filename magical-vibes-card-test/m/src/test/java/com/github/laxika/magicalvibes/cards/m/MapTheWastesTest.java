package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AinokGuide;
import com.github.laxika.magicalvibes.cards.f.FeralKrushok;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MapTheWastes.class, Forest.class, AinokGuide.class, FeralKrushok.class})
class MapTheWastesTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a basic land onto the battlefield tapped and bolsters the least-tough creature")
    void searchesAndBolsters() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new AinokGuide()));
        Permanent leastToughCreature = harness.addToBattlefieldAndReturn(player1, new AinokGuide());
        Permanent largerCreature = harness.addToBattlefieldAndReturn(player1, new FeralKrushok());

        castMapTheWastes();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactly(forest);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(leastToughCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(largerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bolster lets the controller choose among creatures tied for least toughness")
    void choosesAmongTiedCreatures() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AinokGuide());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AinokGuide());

        castMapTheWastes();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.context()).isEqualTo(
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacement(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, true));

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Failing to find a land still bolsters")
    void failingToFindStillBolsters() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AinokGuide());

        castMapTheWastes();
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Map the Wastes");
    }

    @Test
    @DisplayName("An empty library does not prevent bolster")
    void emptyLibraryStillBolsters() {
        harness.setLibrary(player1, List.of());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AinokGuide());

        castMapTheWastes();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Map the Wastes");
    }

    @Test
    @DisplayName("Searching succeeds without own creatures and does not bolster an opponent's creature")
    void noOwnCreaturesStillSearches() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AinokGuide());

        castMapTheWastes();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Map the Wastes");
    }

    @Test
    @DisplayName("Bolster uses current toughness and ignores opposing creatures")
    void usesEffectiveToughness() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new AinokGuide());
        guide.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent krushok = harness.addToBattlefieldAndReturn(player1, new FeralKrushok());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AinokGuide());

        castMapTheWastes();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(krushok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castMapTheWastes() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new MapTheWastes(), "{2}{G}");
    }
}
