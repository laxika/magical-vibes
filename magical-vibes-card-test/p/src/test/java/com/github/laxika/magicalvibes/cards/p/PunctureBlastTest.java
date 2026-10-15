package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChoMannoRevolutionary;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
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

@CardUsed({PunctureBlast.class, GrizzlyBears.class, GiantSpider.class,
        ChoMannoRevolutionary.class, JaceBeleren.class})
class PunctureBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Puncture Blast deals 3 damage to target player")
    void deals3DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PunctureBlast()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Puncture Blast deals 3 damage to a creature as -1/-1 counters (wither)")
    void witherDealsMinusCountersToCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PunctureBlast()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        // 2/2 with three -1/-1 counters dies as a state-based action.
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Wither leaves surviving creature with -1/-1 counters, not marked damage")
    void witherPutsCountersOnSurvivingCreature() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new PunctureBlast()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, spider.getId());

        Permanent resolved = findPermanent(player2, "Giant Spider");
        assertThat(resolved.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(resolved.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Puncture Blast can damage its controller")
    void dealsDamageToController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PunctureBlast()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Prevented wither damage places no counters")
    void preventionStopsWitherCounters() {
        Permanent choManno = harness.addToBattlefieldAndReturn(player2, new ChoMannoRevolutionary());
        harness.setHand(player1, List.of(new PunctureBlast()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, choManno.getId());

        harness.assertOnBattlefield(player2, "Cho-Manno, Revolutionary");
        assertThat(choManno.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(choManno.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Wither damage removes planeswalker loyalty instead of placing -1/-1 counters")
    void damagesPlaneswalker() {
        Permanent jace = harness.enterBattlefieldAndReturn(player2, new JaceBeleren());
        harness.setHand(player1, List.of(new PunctureBlast()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, jace.getId());

        harness.assertNotOnBattlefield(player2, "Jace Beleren");
        harness.assertInGraveyard(player2, "Jace Beleren");
        assertThat(jace.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Wither counters cancel existing +1/+1 counters on your own creature")
    void witherCountersCancelPlusCounters() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new PunctureBlast()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, spider.getId());

        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(spider.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(spider.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot cast Puncture Blast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new PunctureBlast()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
