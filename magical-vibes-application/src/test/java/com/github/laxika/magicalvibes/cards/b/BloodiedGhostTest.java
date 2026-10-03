package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodiedGhost.class})
class BloodiedGhostTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a -1/-1 counter, making the 3/3 a 2/2")
    void entersWithMinusCounter() {
        harness.setHand(player1, List.of(new BloodiedGhost()));
        harness.addMana(player1, ManaColor.WHITE, 3); // {1} + two {W/B}

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent ghost = findPermanent(player1, "Bloodied Ghost");
        assertThat(ghost).isNotNull();
        assertThat(ghost.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(ghost.getEffectivePower()).isEqualTo(2);
        assertThat(ghost.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters with its counter without being cast and without a counter trigger on the stack")
    void entersWithCounterWithoutBeingCast() {
        Permanent ghost = harness.enterBattlefieldAndReturn(player2, new BloodiedGhost());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ghost);
        assertThat(ghost.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(ghost.getEffectivePower()).isEqualTo(2);
        assertThat(ghost.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
