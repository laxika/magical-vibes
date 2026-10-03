package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({AmaliaBenavidesAguirre.class, FountainOfYouth.class, Forest.class,
        GrizzlyBears.class, LlanowarElves.class, ProdigalPyromancer.class, Shock.class})
class AmaliaBenavidesAguirreTest extends BaseCardTest {

    @Test
    @DisplayName("Gaining life makes Amalia explore")
    void gainingLifeMakesAmaliaExplore() {
        Permanent amalia = harness.addToBattlefieldAndReturn(player1, new AmaliaBenavidesAguirre());
        harness.addToBattlefield(player1, new FountainOfYouth());
        GrizzlyBears revealed = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed));

        gainLifeWithFountain();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(amalia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("After exploring, exactly twenty power destroys every other creature")
    void exactPowerAfterExploreDestroysOtherCreatures() {
        Permanent amalia = harness.addToBattlefieldAndReturn(player1, new AmaliaBenavidesAguirre());
        amalia.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 17);
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        gainLifeWithFountain();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(amalia);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(otherCreature);
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gqs.getEffectivePower(gd, amalia)).isEqualTo(20);
    }

    @Test
    @DisplayName("Power above twenty does not destroy other creatures")
    void powerAboveTwentyDoesNotDestroyOtherCreatures() {
        Permanent amalia = harness.addToBattlefieldAndReturn(player1, new AmaliaBenavidesAguirre());
        amalia.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 19);
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setLibrary(player1, List.of(new Forest()));

        gainLifeWithFountain();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherCreature);
        assertThat(gqs.getEffectivePower(gd, amalia)).isEqualTo(21);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they decline the life payment")
    void wardCountersUnpaidSpell() {
        Permanent amalia = harness.addToBattlefieldAndReturn(player1, new AmaliaBenavidesAguirre());
        amalia.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, amalia.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An empty library still gives a counter and can bring Amalia to twenty power")
    void emptyLibraryStillIncreasesPowerAndDestroysOtherCreatures() {
        Permanent amalia = harness.addToBattlefieldAndReturn(player1, new AmaliaBenavidesAguirre());
        amalia.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 17);
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setLibrary(player1, List.of());

        gainLifeWithFountain();

        assertThat(amalia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Amalia Benavides Aguirre");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Exploring a land at twenty power destroys creatures on both sides but spares artifacts")
    void landAtTwentyPowerStillDestroysOtherCreatures() {
        Permanent amalia = harness.addToBattlefieldAndReturn(player1, new AmaliaBenavidesAguirre());
        amalia.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 18);
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        gainLifeWithFountain();

        harness.assertInHand(player1, "Forest");
        assertThat(amalia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Amalia Benavides Aguirre");
        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("A nonland may be put into the graveyard after the counter is added")
    void nonlandCanBePutIntoGraveyard() {
        Permanent amalia = harness.addToBattlefieldAndReturn(player1, new AmaliaBenavidesAguirre());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        gainLifeWithFountain();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(amalia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent gaining life does not make Amalia explore")
    void opponentLifeGainDoesNotTriggerExplore() {
        Permanent amalia = harness.addToBattlefieldAndReturn(player1, new AmaliaBenavidesAguirre());
        harness.addToBattlefield(player2, new FountainOfYouth());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 21);
        assertThat(amalia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying three life for ward lets the opponent's spell resolve")
    void paidWardAllowsSpellToResolve() {
        Permanent amalia = harness.addToBattlefieldAndReturn(player1, new AmaliaBenavidesAguirre());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, amalia.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Amalia Benavides Aguirre");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Amalia still explores and uses her last power after leaving the battlefield")
    void departedAmaliaAtTwentyStillExploresAndDestroysCreatures() {
        Permanent amalia = harness.addToBattlefieldAndReturn(player1, new AmaliaBenavidesAguirre());
        amalia.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 18);
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        amalia.setMarkedDamage(20);
        harness.runStateBasedActions();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Amalia Benavides Aguirre");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Ward also counters an opponent's targeted activated ability")
    void wardCountersUnpaidActivatedAbility() {
        Permanent amalia = harness.addToBattlefieldAndReturn(player1, new AmaliaBenavidesAguirre());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, 0, null, amalia.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(amalia.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Amalia Benavides Aguirre");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Amalia's controller can target her without paying ward")
    void ownSpellDoesNotTriggerWard() {
        Permanent amalia = harness.addToBattlefieldAndReturn(player1, new AmaliaBenavidesAguirre());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, amalia.getId());

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Amalia Benavides Aguirre");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    private void gainLifeWithFountain() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
