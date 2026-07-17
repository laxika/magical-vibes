package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeralHydraTest extends BaseCardTest {

    // ===== Enters with X +1/+1 counters =====

    @Test
    @DisplayName("Casting with X=3 enters with 3 +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new FeralHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3); // 3 generic for X

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findHydra(player1);
        assertThat(hydra).isNotNull();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    // ===== {3}: Put a +1/+1 counter on this creature =====

    @Test
    @DisplayName("Controller pays {3} to add a +1/+1 counter")
    void controllerActivatesCounterAbility() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new FeralHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate without paying the {3} cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new FeralHydra());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Any player may activate =====

    @Test
    @DisplayName("An opponent may pay {3} to add a counter to the controller's Hydra")
    void opponentCanActivate() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new FeralHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.addMana(player2, ManaColor.RED, 3);

        // player2 doesn't control the Hydra but the ability is "any player may activate".
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    private Permanent findHydra(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Feral Hydra"))
                .findFirst().orElse(null);
    }
}
