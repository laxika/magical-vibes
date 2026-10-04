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

        harness.assertNotOnBattlefield(player2, "Wall of Stone");
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

    @Test
    @DisplayName("Ability can destroy a Wall controlled by its controller")
    void destroysOwnWall() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = addCreatureReady(player1, new DwarvenDemolitionTeam());
        Permanent wall = addCreatureReady(player1, new WallOfStone());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(team),
                0, null, wall.getId());
        assertThat(team.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Wall of Stone");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wall of Stone");
        harness.assertInGraveyard(player1, "Wall of Stone");
    }

    @Test
    @DisplayName("Ability cannot activate when the source is already tapped")
    void cannotActivateWhenTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = addCreatureReady(player1, new DwarvenDemolitionTeam());
        Permanent wall = addCreatureReady(player2, new WallOfStone());
        team.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(team), 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Wall of Stone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = addCreatureReady(player1, new DwarvenDemolitionTeam());
        Permanent wall = addCreatureReady(player2, new WallOfStone());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(team),
                0, null, wall.getId());
        gd.playerBattlefields.get(player1.getId()).remove(team);
        gd.playerGraveyards.get(player1.getId()).add(team.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Stone");
        harness.assertInGraveyard(player2, "Wall of Stone");
    }
}
