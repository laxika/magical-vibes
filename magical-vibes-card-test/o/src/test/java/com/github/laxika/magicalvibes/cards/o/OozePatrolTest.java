package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.t.TicketTortoise;
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

@CardUsed({OozePatrol.class, Forest.class, GrizzlyBears.class, PropheticPrism.class, TicketTortoise.class})
class OozePatrolTest extends BaseCardTest {

    private Permanent castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OozePatrol()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        return findPermanent(player1, "Ooze Patrol");
    }

    @Test
    @DisplayName("ETB mills two and counts artifact and creature cards in the graveyard")
    void etbMillsTwoAndCountsArtifactAndCreatureCards() {
        harness.setGraveyard(player1, List.of(new PropheticPrism()));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new PropheticPrism(), new Forest()));

        Permanent ooze = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB puts no counters when the graveyard has no artifact or creature cards")
    void etbPutsNoCountersForNonmatchingGraveyard() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        Permanent ooze = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Artifact creatures count once and opposing graveyards do not count")
    void artifactCreaturesCountOnceInControllersGraveyard() {
        harness.setGraveyard(player1, List.of(new TicketTortoise()));
        harness.setGraveyard(player2, List.of(new TicketTortoise(), new OozePatrol()));
        harness.setLibrary(player1, List.of(new TicketTortoise(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        Permanent ooze = castAndResolveEtb();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A short library mills all available cards and still adds counters")
    void shortLibraryStillAddsCounters() {
        harness.setGraveyard(player1, List.of(new OozePatrol()));
        harness.setLibrary(player1, List.of(new TicketTortoise()));

        Permanent ooze = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An empty library does not prevent counting the existing graveyard")
    void emptyLibraryStillCountsExistingGraveyard() {
        harness.setGraveyard(player1, List.of(new TicketTortoise()));
        harness.setLibrary(player1, List.of());

        Permanent ooze = castAndResolveEtb();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }
}
