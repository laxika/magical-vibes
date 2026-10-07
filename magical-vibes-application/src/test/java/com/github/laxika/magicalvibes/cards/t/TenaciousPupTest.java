package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TenaciousPup.class, GrizzlyBears.class})
class TenaciousPupTest extends BaseCardTest {

    @Test
    void gainsLifeAndEmpowersTheNextCreatureSpellOnce() {
        TenaciousPup pup = new TenaciousPup();
        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(pup, firstBears));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 21);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent empowered = findPermanent(player1, "Grizzly Bears");
        assertThat(empowered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(empowered.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(empowered.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, empowered, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, empowered, Keyword.VIGILANCE)).isTrue();

        harness.setHand(player1, List.of(secondBears));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent unempowered = findPermanents(player1, "Grizzly Bears").get(1);
        assertThat(unempowered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(unempowered.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(unempowered.getCounterCount(CounterType.VIGILANCE)).isZero();
    }

    @Test
    void boonSurvivesItsSourceLeavingTheBattlefield() {
        harness.setHand(player1, List.of(new TenaciousPup(), new TenaciousPup()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent source = findPermanent(player1, "Tenacious Pup");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, source));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent empowered = findPermanent(player1, "Tenacious Pup");
        assertThat(empowered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(empowered.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(empowered.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        harness.assertLife(player1, 22);
    }

    @Test
    void boonPersistsAcrossTurnsAndIgnoresOpponentsCreatureSpells() {
        harness.setLibrary(player1, List.of(new TenaciousPup(), new TenaciousPup()));
        harness.setLibrary(player2, List.of(new TenaciousPup(), new TenaciousPup()));
        harness.setHand(player1, List.of(new TenaciousPup()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new TenaciousPup()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castCreature(player2, 0);
        resolveAllTriggers();
        Permanent opponentsPup = findPermanent(player2, "Tenacious Pup");
        assertThat(opponentsPup.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentsPup.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(opponentsPup.getCounterCount(CounterType.VIGILANCE)).isZero();

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TenaciousPup()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent empowered = findPermanents(player1, "Tenacious Pup").get(1);
        assertThat(empowered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(empowered.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(empowered.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
    }

    @Test
    void enteringWithoutCastingDoesNotConsumeBoonAndMultipleBoonsStack() {
        harness.setHand(player1, List.of(new TenaciousPup()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent uncastPup = harness.enterBattlefieldAndReturn(player1, new TenaciousPup());
        resolveAllTriggers();
        assertThat(uncastPup.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(uncastPup.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(uncastPup.getCounterCount(CounterType.VIGILANCE)).isZero();

        harness.setHand(player1, List.of(new TenaciousPup()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent empowered = findPermanents(player1, "Tenacious Pup").get(2);
        assertThat(empowered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(empowered.getCounterCount(CounterType.TRAMPLE)).isEqualTo(2);
        assertThat(empowered.getCounterCount(CounterType.VIGILANCE)).isEqualTo(2);
        harness.assertLife(player1, 23);
    }
}
