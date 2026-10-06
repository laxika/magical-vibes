package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.cards.e.EpitaphGolem;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndulgentAristocrat.class, CaptivatingVampire.class, EpitaphGolem.class})
class IndulgentAristocratTest extends BaseCardTest {

    @Test
    @DisplayName("{2}, sacrifice a creature: each Vampire you control gets a +1/+1 counter")
    void abilityPutsCounterOnEachVampire() {
        Permanent aristocrat = addCreatureReady(player1, new IndulgentAristocrat());
        Permanent vampire = addCreatureReady(player1, new CaptivatingVampire());
        Permanent golem = addCreatureReady(player1, new EpitaphGolem());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, golem.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Epitaph Golem");
        assertThat(aristocrat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Vampire creatures you control do not get a counter")
    void nonVampireDoesNotGetCounter() {
        Permanent aristocrat = addCreatureReady(player1, new IndulgentAristocrat());
        Permanent golem = addCreatureReady(player1, new EpitaphGolem());
        Permanent fodder = addCreatureReady(player1, new EpitaphGolem());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(aristocrat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("May sacrifice itself; other Vampires still get counters")
    void canSacrificeItself() {
        Permanent aristocrat = addCreatureReady(player1, new IndulgentAristocrat());
        Permanent vampire = addCreatureReady(player1, new CaptivatingVampire());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, aristocrat.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Indulgent Aristocrat");
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opposing Vampires do not receive counters")
    void opposingVampiresDoNotGetCounters() {
        Permanent aristocrat = addCreatureReady(player1, new IndulgentAristocrat());
        Permanent opponent = addCreatureReady(player2, new IndulgentAristocrat());
        Permanent fodder = addCreatureReady(player1, new EpitaphGolem());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.assertNotOnBattlefield(player1, "Epitaph Golem");
        harness.assertInGraveyard(player1, "Epitaph Golem");
        assertThat(aristocrat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(aristocrat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Tapped, summoning-sick Aristocrat can activate its ability")
    void abilityDoesNotRequireTappingOrHaste() {
        Permanent aristocrat = addCreatureReady(player1, new IndulgentAristocrat());
        aristocrat.setSummoningSick(true);
        aristocrat.tap();
        Permanent fodder = addCreatureReady(player1, new EpitaphGolem());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(aristocrat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage gains life through lifelink")
    void combatDamageGainsLife() {
        addCreatureReady(player1, new IndulgentAristocrat());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }
}
