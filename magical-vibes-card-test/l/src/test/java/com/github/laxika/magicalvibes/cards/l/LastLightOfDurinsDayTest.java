package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.SmaugWickedWorm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LastLightOfDurinsDay.class, SmaugWickedWorm.class, Mountain.class, Forest.class, NamelessInversion.class,
        PsychogenicProbe.class})
class LastLightOfDurinsDayTest extends BaseCardTest {

    @Test
    @DisplayName("A Mountain adds the sixth quest counter, sacrifices the enchantment, and searches hand or library")
    void mountainReachesSixCountersAndFindsDragon() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        light.setCounterCount(CounterType.QUEST, 5);
        SmaugWickedWorm dragon = new SmaugWickedWorm();
        harness.setHand(player1, List.of(new Mountain(), dragon));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Last Light of Durin's Day");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == dragon);
    }

    @Test
    @DisplayName("A non-Mountain land does not add a quest counter")
    void nonMountainDoesNotTrigger() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(light.getCounterCount(CounterType.QUEST)).isZero();
        harness.assertOnBattlefield(player1, "Last Light of Durin's Day");
    }

    @Test
    @DisplayName("A Dragon in the graveyard is not eligible for the search")
    void graveyardDragonIsNotEligible() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        light.setCounterCount(CounterType.QUEST, 5);
        SmaugWickedWorm dragon = new SmaugWickedWorm();
        harness.setGraveyard(player1, List.of(dragon));
        harness.setHand(player1, List.of(new Mountain()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Smaug, Wicked Worm");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == dragon);
    }

    @Test
    @DisplayName("Mountaincycling discards this card and offers only Mountains")
    void mountaincyclingSearchesForMountain() {
        harness.setHand(player1, List.of(new LastLightOfDurinsDay()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Mountain mountain = new Mountain();
        harness.setLibrary(player1, List.of(mountain, new Forest()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Last Light of Durin's Day");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(mountain);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(mountain);
    }

    @Test
    @DisplayName("A Mountain below six quest counters adds one without sacrificing the enchantment")
    void mountainBelowThresholdAddsOneCounterWithoutSacrificing() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        light.setCounterCount(CounterType.QUEST, 4);

        harness.enterBattlefieldAndReturn(player1, new Mountain());
        harness.passBothPriorities();

        assertThat(light.getCounterCount(CounterType.QUEST)).isEqualTo(5);
        harness.assertOnBattlefield(player1, "Last Light of Durin's Day");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Mountain adds no quest counter")
    void opponentsMountainDoesNotTrigger() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());

        harness.enterBattlefieldAndReturn(player2, new Mountain());
        harness.passBothPriorities();

        assertThat(light.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("With more than six quest counters it still sacrifices and finds a Dragon in the library")
    void moreThanSixCountersStillFindsDragonInLibrary() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        light.setCounterCount(CounterType.QUEST, 7);
        SmaugWickedWorm dragon = new SmaugWickedWorm();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(dragon, new Forest()));

        harness.enterBattlefieldAndReturn(player1, new Mountain());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        harness.assertInGraveyard(player1, "Last Light of Durin's Day");
        harness.assertOnBattlefield(player1, "Smaug, Wicked Worm");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(dragon);
        assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("Choosing to search only the hand neither searches nor shuffles the library")
    void searchingOnlyHandDoesNotSearchOrShuffleLibrary() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        light.setCounterCount(CounterType.QUEST, 5);
        SmaugWickedWorm dragon = new SmaugWickedWorm();
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        harness.setHand(player1, List.of(dragon));
        harness.setLibrary(player1, List.of(forest, mountain));

        harness.enterBattlefieldAndReturn(player1, new Mountain());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        harness.assertOnBattlefield(player1, "Smaug, Wicked Worm");
        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, mountain);
    }

    @Test
    @DisplayName("Choosing to search the library with no Dragon anywhere still searches it, so nothing is revealed")
    void fruitlessSearchStillSearchesLibrary() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        light.setCounterCount(CounterType.QUEST, 5);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new Mountain());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Last Light of Durin's Day");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("A nonpermanent Dragon card in hand is not offered")
    void nonpermanentDragonCardCannotBeChosenForBattlefield() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        light.setCounterCount(CounterType.QUEST, 5);
        NamelessInversion instant = new NamelessInversion();
        SmaugWickedWorm dragon = new SmaugWickedWorm();
        harness.setHand(player1, List.of(instant, dragon));
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new Mountain());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class).pool())
                .containsExactly(dragon);
    }

    @Nested
    @DisplayName("Choosing the zones of \"your hand and/or library\"")
    class ZoneChoice {

        @Test
        @DisplayName("Asks whether to search the library before any card is shown")
        void asksBeforeShowingCards() {
            SmaugWickedWorm dragon = new SmaugWickedWorm();
            reachSixCounters(List.of(dragon), List.of(new Forest()));

            assertThat(gd.interaction.activeInteraction())
                    .isNotInstanceOf(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
            assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Searching only the hand may find nothing, and the library is neither searched nor shuffled")
        void searchingOnlyTheHandMayFindNothing() {
            SmaugWickedWorm dragon = new SmaugWickedWorm();
            SmaugWickedWorm libraryDragon = new SmaugWickedWorm();
            reachSixCounters(List.of(dragon), List.of(libraryDragon));
            harness.addToBattlefield(player2, new PsychogenicProbe());

            harness.handleMayAbilityChosen(player1, false);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class)
                    .pool()).containsExactly(dragon);
            harness.handleMultipleCardsChosen(player1, List.of());

            harness.assertInHand(player1, "Smaug, Wicked Worm");
            assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player1.getId());
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Searching the library too and taking the Dragon from the hand still searches and shuffles it")
        void searchingTheLibraryTooCountsWhateverIsFound() {
            SmaugWickedWorm dragon = new SmaugWickedWorm();
            reachSixCounters(List.of(dragon), List.of(new Forest(), new Mountain()));
            harness.addToBattlefield(player2, new PsychogenicProbe());
            harness.setLife(player1, 20);

            harness.handleMayAbilityChosen(player1, true);
            harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

            harness.assertOnBattlefield(player1, "Smaug, Wicked Worm");
            assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player1.getId());
            harness.passBothPriorities();
            harness.assertLife(player1, 18);
        }

        private void reachSixCounters(List<Card> hand, List<Card> library) {
            Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
            light.setCounterCount(CounterType.QUEST, 5);
            harness.setHand(player1, hand);
            harness.setLibrary(player1, library);
            harness.enterBattlefieldAndReturn(player1, new Mountain());
            harness.passBothPriorities();
        }
    }
}
