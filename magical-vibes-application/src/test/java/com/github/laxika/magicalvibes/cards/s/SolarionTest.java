package com.github.laxika.magicalvibes.cards.s;

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

class SolarionTest extends BaseCardTest {

    @Test
    @DisplayName("Sunburst counts each color of mana spent to cast Solarion once")
    void sunburstCountsDistinctColors() {
        harness.setHand(player1, List.of(new Solarion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent solarion = findPermanent(player1, "Solarion");

        assertThat(solarion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(solarion.getEffectivePower()).isEqualTo(5);
        assertThat(solarion.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Tapping Solarion doubles its +1/+1 counters")
    void tappingDoublesCounters() {
        Permanent solarion = addReadySolarion(player1);
        solarion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        prepareTurn();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(solarion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(solarion.isTapped()).isTrue();
    }

    private void prepareTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addReadySolarion(Player player) {
        Permanent permanent = new Permanent(new Solarion());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
