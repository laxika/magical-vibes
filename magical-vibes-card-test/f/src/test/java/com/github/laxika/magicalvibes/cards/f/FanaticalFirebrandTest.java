package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AjaniCallerOfThePride;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FanaticalFirebrand.class, GrizzlyBears.class, LlanowarElves.class, AjaniCallerOfThePride.class})
class FanaticalFirebrandTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 1 damage to a player")
    void sacrificesAndDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyFirebrand(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Fanatical Firebrand");
        harness.assertInGraveyard(player1, "Fanatical Firebrand");

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature")
    void dealsDamageToTargetCreature() {
        addReadyFirebrand(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 1 damage to a 1/1 target creature")
    void dealsDamageToOneToughnessCreature() {
        addReadyFirebrand(player1);
        harness.addToBattlefield(player2, new LlanowarElves());
        Permanent target = findPermanent(player2, "Llanowar Elves");

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Haste permits activation on the turn it enters")
    void activatesWhileSummoningSick() {
        harness.enterBattlefieldAndReturn(player1, new FanaticalFirebrand());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Fanatical Firebrand");
    }

    @Test
    @DisplayName("A tapped Firebrand cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        Permanent firebrand = addReadyFirebrand(player1);
        firebrand.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Fanatical Firebrand");
        harness.assertNotInGraveyard(player1, "Fanatical Firebrand");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target its controller")
    void canDamageItsController() {
        addReadyFirebrand(player1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target itself but its ability fizzles after sacrificing it")
    void canTargetItself() {
        Permanent firebrand = addReadyFirebrand(player1);

        harness.activateAbility(player1, 0, null, firebrand.getId());

        harness.assertInGraveyard(player1, "Fanatical Firebrand");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals damage to a planeswalker by removing loyalty")
    void dealsDamageToPlaneswalker() {
        addReadyFirebrand(player1);
        Permanent ajani = harness.enterBattlefieldAndReturn(player2, new AjaniCallerOfThePride());

        harness.activateAbility(player1, 0, null, ajani.getId());
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Ajani, Caller of the Pride");
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyFirebrand(Player player) {
        return addCreatureReady(player, new FanaticalFirebrand());
    }
}
