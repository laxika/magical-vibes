package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VraskaTheUnseen;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhituFireEater.class, GrizzlyBears.class})
class GhituFireEaterTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage (its power) to target player")
    void dealsPowerDamageToPlayer() {
        addReadyFireEater(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals damage equal to boosted power to target player")
    void dealsBoostedDamageToPlayer() {
        Permanent fireEater = addReadyFireEater(player1);
        fireEater.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // power becomes 2+2 = 4
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, killing a 2/2")
    void dealsPowerDamageToCreature() {
        addReadyFireEater(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(GrizzlyBears.class::isInstance);
    }

    @Test
    @CardUsed(VraskaTheUnseen.class)
    @DisplayName("Deals 2 damage to a target planeswalker")
    void dealsPowerDamageToPlaneswalker() {
        addReadyFireEater(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VraskaTheUnseen());
        target.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot activate the ability while Ghitu Fire-Eater is tapped")
    void cannotActivateWhileTapped() {
        Permanent fireEater = addReadyFireEater(player1);
        fireEater.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fireEater);
    }

    @Test
    @DisplayName("Sacrifices itself as a cost when the ability is activated")
    void sacrificesSelfAsCost() {
        addReadyFireEater(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Ghitu Fire-Eater");
        harness.assertInGraveyard(player1, "Ghitu Fire-Eater");
    }

    @Test
    @DisplayName("Still deals its last-known power after being sacrificed")
    void dealsLastKnownPowerAfterSacrifice() {
        addReadyFireEater(player1);
        harness.setLife(player2, 20);

        // Sacrifice is paid on activation, so the source is already gone when the
        // ability resolves; it uses the last-known power snapshot (2).
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyFireEater(Player player) {
        return addCreatureReady(player, new GhituFireEater());
    }
}
