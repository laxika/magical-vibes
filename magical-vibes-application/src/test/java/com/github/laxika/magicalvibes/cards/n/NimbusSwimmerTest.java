package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NimbusSwimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=4 enters with 4 +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new NimbusSwimmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 4, null, null);
        harness.passBothPriorities();

        Permanent swimmer = findSwimmer(player1);
        assertThat(swimmer).isNotNull();
        assertThat(swimmer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting with X=0 enters as a 0/0 and dies immediately")
    void entersWithZeroCountersAndDies() {
        harness.setHand(player1, List.of(new NimbusSwimmer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findSwimmer(player1)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nimbus Swimmer"));
    }

    private Permanent findSwimmer(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Nimbus Swimmer"))
                .findFirst().orElse(null);
    }
}
