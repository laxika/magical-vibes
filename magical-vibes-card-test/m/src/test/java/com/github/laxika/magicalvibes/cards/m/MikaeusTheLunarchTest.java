package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.d.DearlyDeparted;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({MikaeusTheLunarch.class, AbbeyGriffin.class, Plains.class, Cloudshift.class, DearlyDeparted.class})
class MikaeusTheLunarchTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with 3 +1/+1 counters")
    void entersWith3Counters() {
        harness.setHand(player1, List.of(new MikaeusTheLunarch()));
        harness.addMana(player1, ManaColor.WHITE, 4); // 1 white + 3 generic for X=3

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent mikaeus = findPermanent(player1, "Mikaeus, the Lunarch");
        assertThat(mikaeus).isNotNull();
        assertThat(mikaeus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=0 enters as 0/0 and dies to state-based actions")
    void entersWith0CountersAndDies() {
        harness.setHand(player1, List.of(new MikaeusTheLunarch()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mikaeus, the Lunarch");
    }

    @Test
    @DisplayName("First ability puts a +1/+1 counter on Mikaeus")
    void firstAbilityPutsCounterOnSelf() {
        Permanent mikaeus = addCreatureReady(player1, new MikaeusTheLunarch());
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(mikaeus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("First ability taps Mikaeus")
    void firstAbilityTapsMikaeus() {
        Permanent mikaeus = addCreatureReady(player1, new MikaeusTheLunarch());
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mikaeus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability puts +1/+1 counter on each other creature you control")
    void secondAbilityDistributesCounters() {
        Permanent mikaeus = addCreatureReady(player1, new MikaeusTheLunarch());
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent griffin1 = addCreatureReady(player1, new AbbeyGriffin());
        Permanent griffin2 = addCreatureReady(player1, new AbbeyGriffin());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Mikaeus had 2 counters, removed 1 as cost, so 1 remaining
        assertThat(mikaeus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // Both griffins get a counter
        assertThat(griffin1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(griffin2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability does NOT put counter on Mikaeus itself")
    void secondAbilityDoesNotCounterSelf() {
        Permanent mikaeus = addCreatureReady(player1, new MikaeusTheLunarch());
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        addCreatureReady(player1, new AbbeyGriffin());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // 3 - 1 cost = 2, should NOT gain a counter from the effect
        assertThat(mikaeus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability does not affect opponent's creatures")
    void secondAbilityDoesNotAffectOpponent() {
        Permanent mikaeus = addCreatureReady(player1, new MikaeusTheLunarch());
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent opponentGriffin = addCreatureReady(player2, new AbbeyGriffin());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(opponentGriffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate second ability without +1/+1 counters")
    void cannotActivateSecondAbilityWithoutCounters() {
        Permanent mikaeus = addCreatureReady(player1, new MikaeusTheLunarch());
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate abilities while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent mikaeus = harness.addToBattlefieldAndReturn(player1, new MikaeusTheLunarch());
        mikaeus.setSummoningSick(true);
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability pays its tap and counter costs before resolving")
    void secondAbilityPaysCostsImmediately() {
        Permanent mikaeus = addCreatureReady(player1, new MikaeusTheLunarch());
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent griffin = addCreatureReady(player1, new AbbeyGriffin());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mikaeus.isTapped()).isTrue();
        assertThat(mikaeus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing the last counter kills Mikaeus but its ability still resolves")
    void secondAbilityResolvesAfterLastCounterIsRemoved() {
        Permanent mikaeus = addCreatureReady(player1, new MikaeusTheLunarch());
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent griffin = addCreatureReady(player1, new AbbeyGriffin());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Mikaeus, the Lunarch");
        harness.assertInGraveyard(player1, "Mikaeus, the Lunarch");
        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability ignores noncreature permanents")
    void secondAbilityDoesNotAffectLands() {
        Permanent mikaeus = addCreatureReady(player1, new MikaeusTheLunarch());
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent griffin = addCreatureReady(player1, new AbbeyGriffin());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(plains.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A blinked Mikaeus receives a counter from its previous object's ability")
    void secondAbilityIncludesReturnedMikaeus() {
        harness.setGraveyard(player1, List.of(new DearlyDeparted()));
        Permanent mikaeus = addCreatureReady(player1, new MikaeusTheLunarch());
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent griffin = addCreatureReady(player1, new AbbeyGriffin());
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.castInstant(player1, 0, mikaeus.getId());
        harness.passBothPriorities();

        Permanent returnedMikaeus = findPermanent(player1, "Mikaeus, the Lunarch");
        assertThat(returnedMikaeus.getId()).isNotEqualTo(mikaeus.getId());
        assertThat(returnedMikaeus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(returnedMikaeus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
