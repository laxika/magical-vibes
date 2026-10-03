package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Murder;
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

@CardUsed({DapperShieldmate.class, Shock.class, Murder.class})
class DapperShieldmateTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a shield counter")
    void entersWithShieldCounter() {
        Permanent shieldmate = castShieldmate();

        assertThat(shieldmate.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters with its shield immediately even when not cast")
    void entersWithoutCastingWithShieldAndNoTrigger() {
        Permanent shieldmate = harness.enterBattlefieldAndReturn(player1, new DapperShieldmate());

        assertThat(shieldmate.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
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
        harness.castAndResolveInstant(player1, 0, shieldmate.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shieldmate);
        assertThat(shieldmate.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(shieldmate.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A second damage event kills it after its shield is consumed")
    void secondDamageEventKillsAfterShieldIsConsumed() {
        Permanent shieldmate = castShieldmate();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, shieldmate.getId());

        assertThat(shieldmate.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(shieldmate.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Dapper Shieldmate");

        harness.castAndResolveInstant(player1, 0, shieldmate.getId());

        harness.assertNotOnBattlefield(player1, "Dapper Shieldmate");
        harness.assertInGraveyard(player1, "Dapper Shieldmate");
    }

    @Test
    @DisplayName("A shield prevents destruction only once and does not tap the creature")
    void shieldCounterReplacesDestructionOnlyOnce() {
        Permanent shieldmate = castShieldmate();

        harness.setHand(player1, List.of(new Murder(), new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, shieldmate.getId());

        harness.assertOnBattlefield(player1, "Dapper Shieldmate");
        assertThat(shieldmate.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(shieldmate.isTapped()).isFalse();

        harness.castAndResolveInstant(player1, 0, shieldmate.getId());

        harness.assertNotOnBattlefield(player1, "Dapper Shieldmate");
        harness.assertInGraveyard(player1, "Dapper Shieldmate");
    }

    @Test
    @DisplayName("The power bonus follows each Shieldmate's controller as the turn changes")
    void powerBonusUpdatesForBothControllersWhenTurnChanges() {
        Permanent ownShieldmate = harness.addToBattlefieldAndReturn(player1, new DapperShieldmate());
        Permanent opposingShieldmate = harness.addToBattlefieldAndReturn(player2, new DapperShieldmate());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, ownShieldmate)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingShieldmate)).isEqualTo(2);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, ownShieldmate)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingShieldmate)).isEqualTo(4);

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, ownShieldmate)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingShieldmate)).isEqualTo(2);
    }

    private Permanent castShieldmate() {
        harness.castFromHand(player1, new DapperShieldmate(), "{3}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Dapper Shieldmate");
    }
}
