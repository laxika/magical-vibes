package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BoggartRamGang;
import com.github.laxika.magicalvibes.cards.b.BarrentonMedic;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.cards.t.Tatterkite;
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

@CardUsed({PunctureBolt.class, BoggartRamGang.class, SafeholdSentry.class, Mountain.class,
        Tatterkite.class, BarrentonMedic.class})
class PunctureBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Still puts a counter on the creature when all damage is prevented")
    void putsCounterWhenDamageIsPrevented() {
        addCreatureReady(player1, new BarrentonMedic());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new PunctureBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Safehold Sentry");
    }

    @Test
    @DisplayName("Places the counter before a creature with lethal damage dies")
    void placesCounterBeforeLethalDamageIsChecked() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());
        target.setMarkedDamage(1);
        harness.setHand(player1, List.of(new PunctureBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Safehold Sentry");
        harness.assertInGraveyard(player2, "Safehold Sentry");
    }

    @Test
    @DisplayName("Deals 1 damage and puts a -1/-1 counter on the target creature")
    void dealsDamageAndPutsCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BoggartRamGang()); // 3/3
        harness.setHand(player1, List.of(new PunctureBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills a 2/2: -1/-1 counter plus 1 damage is lethal")
    void killsTwoTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry()); // 2/2 -> 1/1 with 1 damage
        harness.setHand(player1, List.of(new PunctureBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Safehold Sentry");
        harness.assertInGraveyard(player2, "Safehold Sentry");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new PunctureBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot put a -1/-1 counter on Tatterkite")
    void respectsCantHaveCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Tatterkite()); // 2/1
        harness.setHand(player1, List.of(new PunctureBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Tatterkite");
    }
}
