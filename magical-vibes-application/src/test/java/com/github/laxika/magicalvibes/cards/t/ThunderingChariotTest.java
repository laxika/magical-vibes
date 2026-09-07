package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderingChariot.class, GrizzlyBears.class})
class ThunderingChariotTest extends BaseCardTest {

    @Test
    void crewAnimatesChariotAndTapsCrew() {
        Permanent chariot = addReadyChariot(player1);
        Permanent crew = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, chariot)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent chariot = addReadyChariot(player1);
        addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, chariot)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, chariot)).isFalse();
    }

    private Permanent addReadyChariot(Player player) {
        Permanent permanent = new Permanent(new ThunderingChariot());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
