package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApocalypseRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Grants lifelink and unblockable to a qualifying creature")
    void grantsLifelinkAndUnblockable() {
        Permanent runner = addRunnerReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        assertThat(bears.isCantBeBlocked()).isTrue();
        assertThat(runner.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Temporary abilities wear off at cleanup")
    void temporaryAbilitiesWearOff() {
        addRunnerReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
        assertThat(bears.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Only targets creatures you control with power 2 or less")
    void rejectsIllegalTargets() {
        addRunnerReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent largeCreature = addCreatureReady(player1, new HillGiant());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, largeCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Crew 3 animates Apocalypse Runner and taps the crew")
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent runner = addRunnerReady(player1);
        Permanent crew = addCreatureReady(player1, new HillGiant());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, runner)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addRunnerReady(Player player) {
        Permanent runner = new Permanent(new ApocalypseRunner());
        runner.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(runner);
        return runner;
    }
}
