package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UntetheredExpressTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent express = addUntetheredExpressReady(player1);

        assertThat(gqs.isCreature(gd, express)).isFalse();
    }

    @Test
    void crewOneAnimatesExpressAndTapsTheCrew() {
        Permanent express = addUntetheredExpressReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, express)).isTrue();
        assertThat(gqs.getEffectivePower(gd, express)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, express)).isEqualTo(4);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void attackingPutsACounterOnExpress() {
        Permanent express = addUntetheredExpressReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(express.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, express)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, express)).isEqualTo(5);
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addUntetheredExpressReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    private Permanent addUntetheredExpressReady(Player player) {
        Permanent permanent = new Permanent(new UntetheredExpress());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
