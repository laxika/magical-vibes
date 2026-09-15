package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShockTroops.class, FreshVolunteers.class})
class ShockTroopsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Shock Troops deals 2 damage to target player")
    void dealsDamageToPlayer() {
        addCreatureReady(player1, new ShockTroops());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Any target includes Shock Troops's controller")
    void dealsDamageToItsController() {
        addCreatureReady(player1, new ShockTroops());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Can activate while summoning sick because the ability does not require tapping")
    void canActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ShockTroops());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Shock Troops is sacrificed as part of the cost")
    void sacrificedAsCost() {
        addCreatureReady(player1, new ShockTroops());

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Shock Troops");
        harness.assertInGraveyard(player1, "Shock Troops");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, destroying a 2/2")
    void dealsDamageToCreatureKilling2Toughness() {
        addCreatureReady(player1, new ShockTroops());
        harness.addToBattlefield(player2, new FreshVolunteers());

        Permanent target = findPermanent(player2, "Fresh Volunteers");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new ShockTroops());
        harness.addToBattlefield(player2, new FreshVolunteers());

        Permanent target = findPermanent(player2, "Fresh Volunteers");
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

}
