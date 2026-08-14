package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NineLivesFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with eight revival counters when cast")
    void entersWithCountersWhenCast() {
        harness.setHand(player1, List.of(new NineLivesFamiliar()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent familiar = findPermanent(player1, "Nine-Lives Familiar");
        assertThat(familiar.getCounterCount(CounterType.REVIVAL)).isEqualTo(8);
    }

    @Test
    @DisplayName("Returns at the next end step with one fewer revival counter")
    void returnsWithOneFewerCounter() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new NineLivesFamiliar());
        familiar.setCounterCount(CounterType.REVIVAL, 3);
        kill(familiar);

        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Nine-Lives Familiar");
        assertThat(returned.getCounterCount(CounterType.REVIVAL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not return when it dies without a revival counter")
    void doesNotReturnWithoutCounter() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new NineLivesFamiliar());
        kill(familiar);

        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(findPermanentOrNull(player1, "Nine-Lives Familiar")).isNull();
        harness.assertInGraveyard(player1, "Nine-Lives Familiar");
    }

    private void kill(Permanent familiar) {
        familiar.setMarkedDamage(familiar.getEffectiveToughness());
        harness.runStateBasedActions();
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private Permanent findPermanentOrNull(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
