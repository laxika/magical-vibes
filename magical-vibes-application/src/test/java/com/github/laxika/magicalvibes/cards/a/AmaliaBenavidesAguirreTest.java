package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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
        GrizzlyBears.class, LlanowarElves.class, Shock.class})
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
        Permanent amalia = new Permanent(new AmaliaBenavidesAguirre());
        amalia.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(amalia);

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

    private void gainLifeWithFountain() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
