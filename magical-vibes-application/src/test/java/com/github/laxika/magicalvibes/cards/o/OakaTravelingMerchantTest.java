package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OakaTravelingMerchant.class, GrizzlyBears.class, Forest.class})
class OakaTravelingMerchantTest extends BaseCardTest {

    @Test
    @DisplayName("Removes a counter from a nonland permanent and draws a card")
    void removesCounterFromNonlandPermanentAndDraws() {
        Permanent oaka = addReadyOaka();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        activateOaka(oaka);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Cannot remove a counter from a land")
    void rejectsLand() {
        Permanent oaka = addReadyOaka();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> activateOaka(oaka))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
        assertThat(land.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    private Permanent addReadyOaka() {
        Permanent oaka = new Permanent(new OakaTravelingMerchant());
        oaka.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(oaka);
        return oaka;
    }

    private void activateOaka(Permanent oaka) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oaka);
        harness.activateAbility(player1, permanentIndex, 0, null, null);
        harness.passBothPriorities();
    }
}
