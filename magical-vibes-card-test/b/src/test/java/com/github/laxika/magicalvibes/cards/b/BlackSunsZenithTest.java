package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FangrenMarauder;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.r.Recoup;
import com.github.laxika.magicalvibes.cards.r.Reverberate;
import com.github.laxika.magicalvibes.cards.s.SphereOfTheSuns;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackSunsZenith.class, GrizzlyBears.class, FangrenMarauder.class,
        SphereOfTheSuns.class, Reverberate.class, PsychogenicProbe.class, Recoup.class})
class BlackSunsZenithTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=3: {3}{B}{B} = 5

        harness.castSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("X=2 puts two -1/-1 counters on each creature")
    void putsXCountersOnEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=2: {2}{B}{B} = 4

        harness.castAndResolveSorcery(player1, 0, 2);

        // Both 2/2 bears get 2 -1/-1 counters → 0/0 → die to SBA
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("X=1 puts one -1/-1 counter, small creatures survive with reduced stats")
    void x1ReducesButDoesNotKillBears() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2

        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=1: {1}{B}{B} = 3

        harness.castAndResolveSorcery(player1, 0, 1);

        // Bear is now 1/1 (2/2 with one -1/-1 counter)
        assertThat(bear.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Large X kills small creatures but not large ones")
    void largeXKillsSmallButNotLargeCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        Permanent bigCreature = harness.addToBattlefieldAndReturn(player2, new FangrenMarauder()); // 5/5

        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=3: {3}{B}{B} = 5

        harness.castAndResolveSorcery(player1, 0, 3);

        // 2/2 bear gets 3 -1/-1 counters → -1/-1 → dies
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        // 5/5 gets 3 -1/-1 counters → 2/2 → survives
        harness.assertOnBattlefield(player2, "Fangren Marauder");
        assertThat(bigCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("X=0 resolves with no counters placed")
    void xZeroPlacesNoCounters() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 2); // X=0: {0}{B}{B} = 2

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(bear.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Counters are permanent and persist across turns")
    void countersPersistAcrossTurns() {
        Permanent bigCreature = harness.addToBattlefieldAndReturn(player1, new FangrenMarauder()); // 5/5

        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=2

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(bigCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);

        // Counters still there after moving to next turn
        advanceToUpkeep(player2);
        assertThat(bigCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Black Sun's Zenith is shuffled into library instead of going to graveyard")
    void shuffledIntoLibraryNotGraveyard() {
        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=1

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 1);

        GameData gd = harness.getGameData();
        // Not in graveyard
        harness.assertNotInGraveyard(player1, "Black Sun's Zenith");
        // In library (deck size increased by 1)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        // Card exists somewhere in the deck
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Black Sun's Zenith"));
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Noncreature artifacts do not receive -1/-1 counters")
    void doesNotPutCountersOnNoncreatures() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new SphereOfTheSuns());
        sphere.setCounterCount(CounterType.CHARGE, 3);
        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertOnBattlefield(player1, "Sphere of the Suns");
        assertThat(sphere.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(sphere.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("A copied Zenith shuffles the copy owner's library and triggers shuffle abilities")
    void copyStillShufflesLibrary() {
        BlackSunsZenith zenith = new BlackSunsZenith();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setHand(player1, List.of(zenith));
        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.RED, 2);
        int librarySize = gd.playerDecks.get(player2.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, zenith.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("A flashed-back Zenith is exiled and cannot also remain in its owner's library")
    void flashbackExilesInsteadOfPuttingCardInLibrary() {
        BlackSunsZenith zenith = new BlackSunsZenith();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(zenith));
        harness.setHand(player1, List.of(new Recoup()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, zenith.getId());
        harness.addMana(player1, ManaColor.BLACK, 3);
        int librarySize = gd.playerDecks.get(player1.getId()).size();

        harness.castFlashback(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(zenith);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize).doesNotContain(zenith);
        harness.assertNotInGraveyard(player1, "Black Sun's Zenith");
    }
}
