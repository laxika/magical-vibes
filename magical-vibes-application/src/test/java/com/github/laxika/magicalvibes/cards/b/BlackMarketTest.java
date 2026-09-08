package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlackMarketTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a charge counter on itself whenever a creature dies")
    void gainsChargeCounterWhenCreatureDies() {
        Permanent market = addBlackMarket(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        killCreature(player2);

        assertThat(market.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds black mana equal to its charge counters during the first main phase")
    void addsManaForChargeCounters() {
        Permanent market = addBlackMarket(player1);
        market.setCounterCount(CounterType.CHARGE, 3);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    private Permanent addBlackMarket(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BlackMarket());
    }

    private void killCreature(Player victimController) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, harness.getPermanentId(victimController, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
