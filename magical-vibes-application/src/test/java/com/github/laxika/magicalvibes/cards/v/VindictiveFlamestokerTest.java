package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VindictiveFlamestokerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts an oil counter on Vindictive Flamestoker")
    void noncreatureSpellPutsOilCounter() {
        Permanent flamestoker = addFlamestokerReady();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(flamestoker.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not put an oil counter on Vindictive Flamestoker")
    void creatureSpellDoesNotPutOilCounter() {
        Permanent flamestoker = addFlamestokerReady();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(flamestoker.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Oil counters reduce the activation cost and the ability discards then draws four cards")
    void oilCountersReduceActivationCost() {
        Permanent flamestoker = addFlamestokerReady();
        flamestoker.setCounterCount(CounterType.OIL, 2);
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .contains("Shock", "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vindictive Flamestoker");
        harness.assertInGraveyard(player1, "Vindictive Flamestoker");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    private Permanent addFlamestokerReady() {
        return addCreatureReady(player1, new VindictiveFlamestoker());
    }
}
