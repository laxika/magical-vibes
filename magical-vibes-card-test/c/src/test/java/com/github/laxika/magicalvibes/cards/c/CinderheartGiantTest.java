package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.SnakeskinVeil;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CinderheartGiant.class, Murder.class, GrizzlyBears.class, FountainOfYouth.class, SnakeskinVeil.class})
class CinderheartGiantTest extends BaseCardTest {

    private void killGiant(Permanent giant) {
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, giant.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("When it dies, it deals 7 damage to an opponent's randomly chosen creature")
    void deathTriggerDamagesRandomOpponentCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CinderheartGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killGiant(giant);

        harness.assertInGraveyard(player1, "Cinderheart Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The random pool contains only creatures controlled by opponents")
    void randomPoolExcludesControllerCreaturesAndNoncreatures() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CinderheartGiant());
        Permanent friendlyCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());

        killGiant(giant);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(friendlyCreature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FountainOfYouth);
    }

    @Test
    @DisplayName("The death trigger does nothing when no opponent controls a creature")
    void noOpponentCreatureMeansNoDamage() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CinderheartGiant());
        Permanent friendlyCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killGiant(giant);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(friendlyCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The death trigger deals exactly seven damage to a surviving creature")
    void dealsExactlySevenDamage() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CinderheartGiant());
        Permanent opposingGiant = harness.addToBattlefieldAndReturn(player2, new CinderheartGiant());
        opposingGiant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        killGiant(giant);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingGiant);
        assertThat(opposingGiant.getMarkedDamage()).isEqualTo(7);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Only one of multiple eligible creatures takes damage")
    void damagesExactlyOneEligibleCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CinderheartGiant());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CinderheartGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CinderheartGiant());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        killGiant(giant);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(List.of(first.getMarkedDamage(), second.getMarkedDamage()))
                .containsExactlyInAnyOrder(0, 7);
    }

    @Test
    @DisplayName("An opposing creature entering before resolution joins the random pool")
    void choosesCreatureAtResolution() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CinderheartGiant());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, giant.getId());
        harness.assertInGraveyard(player1, "Cinderheart Giant");
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Hexproof does not exclude a creature from the random choice")
    void damagesHexproofCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CinderheartGiant());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new SnakeskinVeil()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, bear.getId());

        killGiant(giant);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
