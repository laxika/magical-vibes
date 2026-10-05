package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({MoldgrafMillipede.class, Forest.class})
class MoldgrafMillipedeTest extends BaseCardTest {

    private Permanent castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MoldgrafMillipede()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB

        return findPermanent(player1, "Moldgraf Millipede");
    }

    @Test
    @DisplayName("ETB mills three and puts counters for milled creatures")
    void etbMillsAndCountersFromMilledCreatures() {
        harness.setLibrary(player1, List.of(
                new MoldgrafMillipede(), new MoldgrafMillipede(), new Forest()));

        Permanent millipede = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(millipede.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB counters count existing graveyard creatures plus milled")
    void etbCountersIncludeExistingGraveyardCreatures() {
        harness.setGraveyard(player1, List.of(new MoldgrafMillipede()));
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest()));

        Permanent millipede = castAndResolveEtb();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(millipede.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB puts no counters when graveyard has no creatures after mill")
    void etbNoCountersWhenNoCreaturesInGraveyard() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest()));

        Permanent millipede = castAndResolveEtb();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(millipede.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB mills only three cards from a larger library")
    void etbLeavesFourthCardInLibrary() {
        Forest fourthCard = new Forest();
        harness.setLibrary(player1, List.of(
                new MoldgrafMillipede(), new Forest(), new Forest(), fourthCard));

        Permanent millipede = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourthCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(millipede.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB mills all available cards from a library smaller than three")
    void etbMillsShortLibraryAndStillAddsCounters() {
        harness.setGraveyard(player1, List.of(new MoldgrafMillipede()));
        harness.setLibrary(player1, List.of(new MoldgrafMillipede(), new Forest()));

        Permanent millipede = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(millipede.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB still counts existing creatures when the library is empty")
    void etbCountsCreaturesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new MoldgrafMillipede(), new Forest()));

        Permanent millipede = castAndResolveEtb();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(millipede.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB neither mills the opponent nor counts their graveyard creatures")
    void etbOnlyUsesControllersZones() {
        Forest opponentTopCard = new Forest();
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.setGraveyard(player2, List.of(new MoldgrafMillipede(), new MoldgrafMillipede()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        Permanent millipede = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(millipede.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
