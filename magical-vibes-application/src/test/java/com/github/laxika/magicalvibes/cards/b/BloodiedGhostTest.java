package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodiedGhostTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a -1/-1 counter, making the 3/3 a 2/2")
    void entersWithMinusCounter() {
        harness.setHand(player1, List.of(new BloodiedGhost()));
        harness.addMana(player1, ManaColor.WHITE, 3); // {1} + two {W/B}

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent ghost = findGhost(player1);
        assertThat(ghost).isNotNull();
        assertThat(ghost.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(ghost.getEffectivePower()).isEqualTo(2);
        assertThat(ghost.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent findGhost(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bloodied Ghost"))
                .findFirst().orElse(null);
    }
}
