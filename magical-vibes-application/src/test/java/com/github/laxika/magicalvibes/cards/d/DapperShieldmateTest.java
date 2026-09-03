package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DapperShieldmate.class, Shock.class})
class DapperShieldmateTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a shield counter")
    void entersWithShieldCounter() {
        Permanent shieldmate = castShieldmate();

        assertThat(shieldmate.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+0 during its controller's turn")
    void getsPowerBonusOnControllerTurn() {
        Permanent shieldmate = harness.addToBattlefieldAndReturn(player1, new DapperShieldmate());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, shieldmate)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shieldmate)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get the power bonus during an opponent's turn")
    void doesNotGetPowerBonusOnOpponentTurn() {
        Permanent shieldmate = harness.addToBattlefieldAndReturn(player1, new DapperShieldmate());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, shieldmate)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shieldmate)).isEqualTo(2);
    }

    @Test
    @DisplayName("Its shield counter prevents one damage event")
    void shieldCounterPreventsDamage() {
        Permanent shieldmate = castShieldmate();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, shieldmate.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shieldmate);
        assertThat(shieldmate.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(shieldmate.getMarkedDamage()).isZero();
    }

    private Permanent castShieldmate() {
        harness.setHand(player1, List.of(new DapperShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Dapper Shieldmate");
    }
}
