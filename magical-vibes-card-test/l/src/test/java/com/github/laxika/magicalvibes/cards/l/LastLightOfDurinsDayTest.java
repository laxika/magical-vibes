package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.SmaugWickedWorm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LastLightOfDurinsDay.class, SmaugWickedWorm.class, Mountain.class, Forest.class, NamelessInversion.class})
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

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SearchHandAndOrLibraryChoice.class);
        harness.assertInGraveyard(player1, "Last Light of Durin's Day");

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
    void opponentsMountainDoesNotTrigger() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());

        harness.enterBattlefieldAndReturn(player2, new Mountain());
        harness.passBothPriorities();

        assertThat(light.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void moreThanSixCountersStillFindsDragonInLibrary() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        light.setCounterCount(CounterType.QUEST, 7);
        SmaugWickedWorm dragon = new SmaugWickedWorm();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(dragon, new Forest()));

        harness.enterBattlefieldAndReturn(player1, new Mountain());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        harness.assertInGraveyard(player1, "Last Light of Durin's Day");
        harness.assertOnBattlefield(player1, "Smaug, Wicked Worm");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(dragon);
        assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player1.getId());
    }

    @Test
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
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        harness.assertOnBattlefield(player1, "Smaug, Wicked Worm");
        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, mountain);
    }

    @Test
    void unsuccessfulSearchMustNotAutomaticallySearchLibrary() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        light.setCounterCount(CounterType.QUEST, 5);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new Mountain());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Last Light of Durin's Day");
        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player1.getId());
    }

    @Test
    void nonpermanentDragonCardCannotBeChosenForBattlefield() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        light.setCounterCount(CounterType.QUEST, 5);
        NamelessInversion instant = new NamelessInversion();
        SmaugWickedWorm dragon = new SmaugWickedWorm();
        harness.setHand(player1, List.of(instant, dragon));
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new Mountain());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchHandAndOrLibraryChoice.class).pool())
                .containsExactly(dragon);
    }
}
