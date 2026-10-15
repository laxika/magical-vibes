package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.Clockspinning;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmenpathToNaya.class, Clockspinning.class})
class OmenpathToNayaTest extends BaseCardTest {

    @Test
    void entersWithFourTimeCounters() {
        harness.setHand(player1, List.of(new OmenpathToNaya()));
        harness.playLand(player1, 0);

        Permanent omenpath = findPermanent(player1, "Omenpath to Naya");

        assertThat(omenpath.getCounterCount(CounterType.TIME)).isEqualTo(4);
    }

    @Test
    void removesOneTimeCounterDuringItsControllersUpkeep() {
        Permanent omenpath = harness.addToBattlefieldAndReturn(player1, new OmenpathToNaya());
        omenpath.setCounterCount(CounterType.TIME, 4);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(omenpath.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(omenpath);
    }

    @Test
    void sacrificesItselfWhenItsLastTimeCounterIsRemoved() {
        Permanent omenpath = harness.addToBattlefieldAndReturn(player1, new OmenpathToNaya());
        omenpath.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Omenpath to Naya");
        harness.assertInGraveyard(player1, "Omenpath to Naya");
    }

    @Test
    void doesNotSacrificeWhenItHasNoTimeCounters() {
        Permanent omenpath = harness.addToBattlefieldAndReturn(player1, new OmenpathToNaya());
        omenpath.setCounterCount(CounterType.TIME, 0);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(omenpath);
    }

    @Test
    void doesNotTriggerDuringUpkeepWithoutTimeCounters() {
        harness.addToBattlefield(player1, new OmenpathToNaya());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Omenpath to Naya");
    }

    @Test
    void doesNotRemoveCountersDuringOpponentsUpkeep() {
        Permanent omenpath = harness.addToBattlefieldAndReturn(player1, new OmenpathToNaya());
        omenpath.setCounterCount(CounterType.TIME, 4);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(omenpath.getCounterCount(CounterType.TIME)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Omenpath to Naya");
    }

    @Test
    @CardUsed({OmenpathToNaya.class, Clockspinning.class})
    void sacrificesWhenAnotherSpellRemovesItsLastTimeCounter() {
        Permanent omenpath = harness.addToBattlefieldAndReturn(player1, new OmenpathToNaya());
        omenpath.setCounterCount(CounterType.TIME, 1);
        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, omenpath.getId());
        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "REMOVE");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Omenpath to Naya");
        harness.assertInGraveyard(player1, "Omenpath to Naya");
    }

    @Test
    void manaAbilityAddsRedGreenOrWhiteMana() {
        harness.addToBattlefield(player1, new OmenpathToNaya());
        harness.addToBattlefield(player1, new OmenpathToNaya());
        harness.addToBattlefield(player1, new OmenpathToNaya());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.activateAbility(player1, 2, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }
}
