package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChimneyRabble;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HazardousBlast;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VindictiveFlamestoker.class, HazardousBlast.class, ChimneyRabble.class, Forest.class, Island.class})
class VindictiveFlamestokerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts an oil counter on Vindictive Flamestoker")
    void noncreatureSpellPutsOilCounter() {
        Permanent flamestoker = addFlamestokerReady();

        harness.castFromHand(player1, new HazardousBlast(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(flamestoker.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not put an oil counter on Vindictive Flamestoker")
    void creatureSpellDoesNotPutOilCounter() {
        Permanent flamestoker = addFlamestokerReady();

        harness.castFromHand(player1, new ChimneyRabble(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(flamestoker.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Oil counters reduce the activation cost and the ability discards then draws four cards")
    void oilCountersReduceActivationCost() {
        Permanent flamestoker = addFlamestokerReady();
        flamestoker.setCounterCount(CounterType.OIL, 2);
        HazardousBlast blast = new HazardousBlast();
        ChimneyRabble rabble = new ChimneyRabble();
        harness.setHand(player1, List.of(blast, rabble));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Vindictive Flamestoker");
        harness.assertInGraveyard(player1, "Vindictive Flamestoker");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blast, rabble);
        harness.assertNotInGraveyard(player1, "Hazardous Blast");
        harness.assertNotInGraveyard(player1, "Chimney Rabble");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vindictive Flamestoker");
        harness.assertInGraveyard(player1, "Vindictive Flamestoker");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Hazardous Blast");
        harness.assertInGraveyard(player1, "Chimney Rabble");
    }

    @Test
    void opponentsNoncreatureSpellDoesNotAddOil() {
        Permanent flamestoker = harness.addToBattlefieldAndReturn(player2, new VindictiveFlamestoker());
        harness.castFromHand(player1, new HazardousBlast(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(flamestoker.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    void noOilCountersRequiresFullCostAndEmptyHandStillDrawsFour() {
        addFlamestokerReady();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough mana");
        harness.assertOnBattlefield(player1, "Vindictive Flamestoker");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vindictive Flamestoker");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void excessOilCountersReduceOnlyGenericManaAndDoNotRequireTapping() {
        Permanent flamestoker = harness.addToBattlefieldAndReturn(player1, new VindictiveFlamestoker());
        flamestoker.setSummoningSick(true);
        flamestoker.tap();
        flamestoker.setCounterCount(CounterType.OIL, 10);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough mana");
        harness.assertOnBattlefield(player1, "Vindictive Flamestoker");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vindictive Flamestoker");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void cardsAddedToHandAfterActivationAreDiscardedOnResolution() {
        Permanent flamestoker = addFlamestokerReady();
        flamestoker.setCounterCount(CounterType.OIL, 6);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new ChimneyRabble()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chimney Rabble");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    private Permanent addFlamestokerReady() {
        return addCreatureReady(player1, new VindictiveFlamestoker());
    }
}
