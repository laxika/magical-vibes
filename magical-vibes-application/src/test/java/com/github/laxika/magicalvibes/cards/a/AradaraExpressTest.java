package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AradaraExpressTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent express = addAradaraExpressReady(player1);

        assertThat(gqs.isCreature(gd, express)).isFalse();
    }

    @Test
    void crewWithEnoughPowerAnimatesExpressAndTapsCrew() {
        Permanent express = addAradaraExpressReady(player1);
        Permanent crew = addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(express.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, express)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addAradaraExpressReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent express = addAradaraExpressReady(player1);
        addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, express)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(express.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, express)).isFalse();
    }

    private Permanent addAradaraExpressReady(Player player) {
        Permanent permanent = new Permanent(new AradaraExpress());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
