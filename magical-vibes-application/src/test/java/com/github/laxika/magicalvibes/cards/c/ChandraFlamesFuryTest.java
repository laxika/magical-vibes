package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChandraFlamesFuryTest extends BaseCardTest {

    @Test
    @DisplayName("+1 deals 2 damage to any target and raises loyalty")
    void plusOneDealsDamageToAnyTarget() {
        Permanent chandra = addReadyChandra(player1, 4);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-2 deals 4 damage to a creature and 2 damage to its controller")
    void minusTwoDamagesCreatureAndController() {
        Permanent chandra = addReadyChandra(player1, 5);
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, spider.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-8 deals 10 damage to a player and each creature they control")
    void minusEightDamagesPlayerAndTheirCreatures() {
        Permanent chandra = addReadyChandra(player1, 8);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 10);
        harness.assertNotOnBattlefield(player1, "Chandra, Flame's Fury");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-8 cannot target a creature")
    void minusEightRequiresPlayerTarget() {
        addReadyChandra(player1, 8);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent perm = new Permanent(new ChandraFlamesFury());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
