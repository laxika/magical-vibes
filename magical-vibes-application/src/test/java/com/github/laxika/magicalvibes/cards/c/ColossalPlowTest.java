package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ColossalPlowTest extends BaseCardTest {

    @Test
    void crewAnimatesPlowAndTapsCreaturesWithTotalPowerSix() {
        Permanent plow = addPlowReady(player1);
        Permanent angel = addCreatureReady(player1, new SerraAngel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(plow.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, plow)).isTrue();
        assertThat(angel.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    void attackingAddsThreePersistentWhiteManaAndGainsThreeLife() {
        harness.setLife(player1, 10);
        addPlowReady(player1);
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player1, new HillGiant());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(3);
        assertThat(pool.getPersistentMana(ManaColor.WHITE)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(3);
    }

    @Test
    void cannotCrewWithoutSixTotalCreaturePower() {
        addPlowReady(player1);
        addCreatureReady(player1, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    private Permanent addPlowReady(Player player) {
        Permanent plow = new Permanent(new ColossalPlow());
        plow.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(plow);
        return plow;
    }
}
