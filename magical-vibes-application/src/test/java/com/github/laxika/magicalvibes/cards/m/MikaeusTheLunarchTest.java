package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MikaeusTheLunarchTest extends BaseCardTest {

    // ===== Enters with X +1/+1 counters =====

    @Test
    @DisplayName("Casting with X=3 enters with 3 +1/+1 counters")
    void entersWith3Counters() {
        harness.setHand(player1, List.of(new MikaeusTheLunarch()));
        harness.addMana(player1, ManaColor.WHITE, 4); // 1 white + 3 generic for X=3

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent mikaeus = findMikaeus(player1);
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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mikaeus, the Lunarch"));
    }

    // ===== First ability: {T}: Put a +1/+1 counter on Mikaeus =====

    @Test
    @DisplayName("First ability puts a +1/+1 counter on Mikaeus")
    void firstAbilityPutsCounterOnSelf() {
        Permanent mikaeus = addReadyMikaeus(player1);
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(mikaeus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("First ability taps Mikaeus")
    void firstAbilityTapsMikaeus() {
        Permanent mikaeus = addReadyMikaeus(player1);
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mikaeus.isTapped()).isTrue();
    }

    // ===== Second ability: distribute counters =====

    @Test
    @DisplayName("Second ability puts +1/+1 counter on each other creature you control")
    void secondAbilityDistributesCounters() {
        Permanent mikaeus = addReadyMikaeus(player1);
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent bear1 = addReadyCreature(player1);
        Permanent bear2 = addReadyCreature(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Mikaeus had 2 counters, removed 1 as cost, so 1 remaining
        assertThat(mikaeus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // Both bears get a counter
        assertThat(bear1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability does NOT put counter on Mikaeus itself")
    void secondAbilityDoesNotCounterSelf() {
        Permanent mikaeus = addReadyMikaeus(player1);
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        addReadyCreature(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // 3 - 1 cost = 2, should NOT gain a counter from the effect
        assertThat(mikaeus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability does not affect opponent's creatures")
    void secondAbilityDoesNotAffectOpponent() {
        Permanent mikaeus = addReadyMikaeus(player1);
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent opponentBear = addReadyCreature(player2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate second ability without +1/+1 counters")
    void cannotActivateSecondAbilityWithoutCounters() {
        Permanent mikaeus = addReadyMikaeus(player1);
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Cannot activate abilities while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent mikaeus = new Permanent(new MikaeusTheLunarch());
        mikaeus.setSummoningSick(true);
        mikaeus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.playerBattlefields.get(player1.getId()).add(mikaeus);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyMikaeus(Player player) {
        Permanent perm = new Permanent(new MikaeusTheLunarch());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findMikaeus(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mikaeus, the Lunarch"))
                .findFirst().orElse(null);
    }
}
