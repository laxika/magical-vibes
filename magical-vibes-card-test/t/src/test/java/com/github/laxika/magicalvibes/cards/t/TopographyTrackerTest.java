package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TopographyTracker.class, GrizzlyBears.class, Forest.class, TurnToFrog.class})
class TopographyTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Map token and makes a creature explore twice")
    void createsMapAndDoublesExplore() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TopographyTracker(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent map = findPermanents(player1, "Map").getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(map), 0,
                null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(firstLand.getId(), secondLand.getId());
        assertThat(findPermanents(player1, "Map")).isEmpty();
    }

    @Test
    void exploresTheSameNonlandTwiceWhenKeptOnTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card nonland = new TopographyTracker();
        harness.setLibrary(player1, List.of(nonland));
        Permanent map = createMap();

        exploreWithMap(map, target);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
    }

    @Test
    void secondExploreUsesTheNextCardAfterGraveyardChoice() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card nonland = new TopographyTracker();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(nonland, land));
        Permanent map = createMap();

        exploreWithMap(map, target);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonland);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
    }

    @Test
    void twoTrackersCauseFourExplores() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TopographyTracker());
        List<Card> lands = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, lands);
        Permanent map = createMap();

        exploreWithMap(map, target);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(lands);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opposingTrackerDoesNotAddAnotherReplacement() {
        harness.addToBattlefield(player2, new TopographyTracker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        Card thirdLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand, thirdLand));
        Permanent map = createMap();

        exploreWithMap(map, target);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstLand, secondLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdLand);
    }

    @Test
    void trackerDoublesItsOwnExplore() {
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        Permanent map = createMap();
        Permanent tracker = findPermanents(player1, "Topography Tracker").getFirst();

        exploreWithMap(map, tracker);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstLand, secondLand);
    }

    @Test
    void emptyLibraryStillExploresTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        Permanent map = createMap();

        exploreWithMap(map, target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void trackerWithNoAbilitiesDoesNotDoubleExplore() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        Permanent map = createMap();
        Permanent tracker = findPermanents(player1, "Topography Tracker").getFirst();
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, tracker.getId());

        exploreWithMap(map, target);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondLand);
    }

    private Permanent createMap() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TopographyTracker(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanents(player1, "Map").getFirst();
    }

    private void exploreWithMap(Permanent map, Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(map), 0,
                null, target.getId());
        harness.passBothPriorities();
    }
}
