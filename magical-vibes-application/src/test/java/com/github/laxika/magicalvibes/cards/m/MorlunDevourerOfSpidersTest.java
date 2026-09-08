package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed(MorlunDevourerOfSpiders.class)
class MorlunDevourerOfSpidersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X counters and deals X damage to the target opponent")
    void entersWithCountersAndDealsDamage() {
        harness.setHand(player1, List.of(new MorlunDevourerOfSpiders()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        Permanent morlun = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(morlun.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("The damage uses the cast-time X value")
    void damageUsesCastTimeXValue() {
        harness.setHand(player1, List.of(new MorlunDevourerOfSpiders()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        Permanent morlun = gd.playerBattlefields.get(player1.getId()).get(0);
        morlun.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new MorlunDevourerOfSpiders()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
