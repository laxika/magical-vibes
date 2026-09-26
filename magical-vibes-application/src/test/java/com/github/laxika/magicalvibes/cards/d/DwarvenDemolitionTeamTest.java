package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenDemolitionTeam.class, WallOfStone.class, GrizzlyBears.class})
class DwarvenDemolitionTeamTest extends BaseCardTest {

    @Test
    @DisplayName("Ability destroys target Wall and taps the source")
    void destroysTargetWall() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = addCreatureReady(player1, new DwarvenDemolitionTeam());
        Permanent wall = addCreatureReady(player2, new WallOfStone());

        int teamIdx = gd.playerBattlefields.get(player1.getId()).indexOf(team);
        harness.activateAbility(player1, teamIdx, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(wall.getId()));
        assertThat(team.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot target a non-Wall creature")
    void cannotTargetNonWall() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = addCreatureReady(player1, new DwarvenDemolitionTeam());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        int teamIdx = gd.playerBattlefields.get(player1.getId()).indexOf(team);
        assertThatThrownBy(() -> harness.activateAbility(player1, teamIdx, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = harness.addToBattlefieldAndReturn(player1, new DwarvenDemolitionTeam());
        Permanent wall = addCreatureReady(player2, new WallOfStone());

        int teamIdx = gd.playerBattlefields.get(player1.getId()).indexOf(team);
        assertThatThrownBy(() -> harness.activateAbility(player1, teamIdx, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
