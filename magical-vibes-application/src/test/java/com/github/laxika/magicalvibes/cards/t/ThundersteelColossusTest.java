package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThundersteelColossus.class, GrizzlyBears.class, LlanowarElves.class})
class ThundersteelColossusTest extends BaseCardTest {

    @Test
    void crewAnimatesColossusAndTapsCrew() {
        Permanent colossus = addColossusReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, colossus)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void crewAnimationEndsAtEndOfTurn() {
        Permanent colossus = addColossusReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, colossus)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, colossus)).isFalse();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addColossusReady(player1);
        addCreatureReady(player1, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    private Permanent addColossusReady(Player player) {
        Permanent colossus = harness.addToBattlefieldAndReturn(player, new ThundersteelColossus());
        colossus.setSummoningSick(false);
        return colossus;
    }
}
