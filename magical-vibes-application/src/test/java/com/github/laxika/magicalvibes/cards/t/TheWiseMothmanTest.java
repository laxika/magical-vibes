package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PersistentPetitioners;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWiseMothman.class, PersistentPetitioners.class, GrizzlyBears.class, Forest.class})
class TheWiseMothmanTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gives each player a rad counter")
    void entersGivesEachPlayerRadCounter() {
        harness.enterBattlefieldAndReturn(player1, new TheWiseMothman());
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player1.getId())).isOne();
        assertThat(gd.playerRadCounters.get(player2.getId())).isOne();
    }

    @Test
    @DisplayName("Attacking gives each player a rad counter")
    void attackingGivesEachPlayerRadCounter() {
        harness.addToBattlefieldAndReturn(player1, new TheWiseMothman()).setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player1.getId())).isOne();
        assertThat(gd.playerRadCounters.get(player2.getId())).isOne();
    }

    @Test
    @DisplayName("Nonland milling puts one counter on each of up to X target creatures")
    void nonlandMillingPutsCountersOnUpToMilledNonlandCountCreatures() {
        Permanent mothman = harness.addToBattlefieldAndReturn(player1, new TheWiseMothman());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent petitioners = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        Permanent petitionersTwo = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        Permanent petitionersThree = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        Permanent petitionersFour = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        petitioners.setSummoningSick(false);
        petitionersTwo.setSummoningSick(false);
        petitionersThree.setSummoningSick(false);
        petitionersFour.setSummoningSick(false);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 3, 1, null, player2.getId());
        resolveAllTriggers();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(mothman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Rad counters mill, cause nonland life loss, and are removed")
    void radCountersMillAndCauseLifeLoss() {
        harness.enterBattlefieldAndReturn(player1, new TheWiseMothman());
        resolveAllTriggers();
        harness.setLife(player1, 20);
        List<com.github.laxika.magicalvibes.model.Card> library =
                List.of(new GrizzlyBears(), new Forest());
        harness.setLibrary(player1, library);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerRadCounters.get(player1.getId())).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(library.get(0));
    }
}
