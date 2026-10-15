package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RalIzzetViceroy;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiremindsResearch.class, GrizzlyBears.class, Shock.class, DirectCurrent.class, RalIzzetViceroy.class})
class FiremindsResearchTest extends BaseCardTest {

    @Test
    void putsAChargeCounterOnItWhenYouCastAnInstantOrSorcery() {
        Permanent research = addResearch();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(research.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForCreatureSpells() {
        Permanent research = addResearch();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(research.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void removesTwoChargeCountersAndDrawsACard() {
        Permanent research = addResearch();
        research.setCounterCount(CounterType.CHARGE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(research.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void removesFiveChargeCountersAndDealsFiveDamageToAnyTarget() {
        Permanent research = addResearch();
        research.setCounterCount(CounterType.CHARGE, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(research.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertLife(player2, 15);
    }

    @Test
    void cannotActivateAnAbilityWithoutEnoughChargeCounters() {
        addResearch();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sorceryCastAddsCounterBeforeSpellResolves() {
        Permanent research = addResearch();
        harness.setHand(player1, List.of(new DirectCurrent()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(research.getCounterCount(CounterType.CHARGE)).isZero();
        harness.passBothPriorities();
        assertThat(research.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        assertThat(research.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void opponentsInstantDoesNotAddCounter() {
        Permanent research = addResearch();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(research.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    void drawingPaysCountersImmediatelyAndPreservesOtherCounters() {
        Permanent research = addResearch();
        research.setCounterCount(CounterType.CHARGE, 3);
        research.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new FiremindsResearch()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(research.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(research.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Firemind's Research");
    }

    @Test
    void damageAbilityCanKillACreatureAndPaysCountersImmediately() {
        Permanent research = addResearch();
        research.setCounterCount(CounterType.CHARGE, 6);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(research.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void damageAbilityCannotSpendFourChargeCountersOrOtherCounterTypes() {
        Permanent research = addResearch();
        research.setCounterCount(CounterType.CHARGE, 4);
        research.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(research.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(research.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void damageAbilityCanTargetAPlaneswalker() {
        Permanent research = addResearch();
        research.setCounterCount(CounterType.CHARGE, 5);
        Permanent ral = harness.addToBattlefieldAndReturn(player2, new RalIzzetViceroy());
        ral.setCounterCount(CounterType.LOYALTY, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, ral.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ral, Izzet Viceroy");
        harness.assertInGraveyard(player2, "Ral, Izzet Viceroy");
        harness.assertLife(player2, 20);
    }

    @Test
    void eachResearchGetsItsOwnCounterFromTheSameSpell() {
        Permanent first = addResearch();
        Permanent second = addResearch();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertLife(player2, 18);
    }

    private Permanent addResearch() {
        return harness.addToBattlefieldAndReturn(player1, new FiremindsResearch());
    }
}
