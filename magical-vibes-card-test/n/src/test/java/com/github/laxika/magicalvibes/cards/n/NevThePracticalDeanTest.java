package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FanningTheFlames;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SteelbaneHydra;
import com.github.laxika.magicalvibes.cards.v.VisceraSeer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NevThePracticalDean.class, FanningTheFlames.class, GrizzlyBears.class,
        SteelbaneHydra.class, VisceraSeer.class})
class NevThePracticalDeanTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control with any counters have trample")
    void creaturesWithAnyCountersHaveTrample() {
        harness.addToBattlefield(player1, new NevThePracticalDean());
        Permanent counteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent uncounteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The first X spell each turn puts X +1/+1 counters on Nev")
    void firstXSpellPutsItsXOnNev() {
        Permanent nev = harness.addToBattlefieldAndReturn(player1, new NevThePracticalDean());
        harness.setHand(player1, List.of(new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());

        assertThat(nev.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nevGainsAndLosesTrampleAsItsCountersChange() {
        Permanent nev = harness.addToBattlefieldAndReturn(player1, new NevThePracticalDean());

        assertThat(gqs.hasKeyword(gd, nev, Keyword.TRAMPLE)).isFalse();
        nev.setCounterCount(CounterType.CHARGE, 1);
        assertThat(gqs.hasKeyword(gd, nev, Keyword.TRAMPLE)).isTrue();
        nev.setCounterCount(CounterType.CHARGE, 0);
        assertThat(gqs.hasKeyword(gd, nev, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void opponentsCounteredCreaturesDoNotGainTrample() {
        harness.addToBattlefield(player1, new NevThePracticalDean());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new VisceraSeer());
        opponentCreature.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void onlyFirstXSpellAddsCountersAndUsesChosenXRatherThanManaSpent() {
        Permanent nev = harness.addToBattlefieldAndReturn(player1, new NevThePracticalDean());
        harness.setHand(player1, List.of(new SteelbaneHydra(), new SteelbaneHydra()));
        harness.addMana(player1, ManaColor.GREEN, 9);

        harness.castAndResolveSorcery(player1, 0, 3);
        assertThat(nev.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nev, Keyword.TRAMPLE)).isTrue();
        harness.passBothPriorities();

        harness.castAndResolveSorcery(player1, 0, 2);
        assertThat(nev.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void castingNonXSpellFirstDoesNotConsumeXTrigger() {
        Permanent nev = harness.addToBattlefieldAndReturn(player1, new NevThePracticalDean());
        harness.setHand(player1, List.of(new VisceraSeer(), new SteelbaneHydra()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(nev.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveSorcery(player1, 0, 3);
        assertThat(nev.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void zeroXSpellConsumesFirstXSpellForTurn() {
        Permanent nev = harness.addToBattlefieldAndReturn(player1, new NevThePracticalDean());
        harness.setHand(player1, List.of(new SteelbaneHydra(), new SteelbaneHydra()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(nev.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        harness.castAndResolveSorcery(player1, 0, 3);
        assertThat(nev.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void xSpellCastBeforeNevEnteredStillCountsAsFirstForTurn() {
        harness.setHand(player1, List.of(new SteelbaneHydra(), new SteelbaneHydra()));
        harness.addMana(player1, ManaColor.GREEN, 9);
        harness.castAndResolveSorcery(player1, 0, 2);

        Permanent nev = harness.addToBattlefieldAndReturn(player1, new NevThePracticalDean());
        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(nev.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentCastingXSpellDoesNotAddCounters() {
        Permanent nev = harness.addToBattlefieldAndReturn(player1, new NevThePracticalDean());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SteelbaneHydra()));
        harness.addMana(player2, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player2, 0, 3);

        assertThat(nev.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void firstXSpellTriggerResetsOnNextTurn() {
        Permanent nev = harness.addToBattlefieldAndReturn(player1, new NevThePracticalDean());
        harness.setHand(player1, List.of(new SteelbaneHydra()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SteelbaneHydra()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(nev.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }
}
