package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CarnivorousPlant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinDiggingTeam.class, CarnivorousPlant.class})
class GoblinDiggingTeamTest extends BaseCardTest {

    @Test
    @DisplayName("Ability destroys target Wall and sacrifices the source")
    void destroysTargetWall() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = addCreatureReady(player1, new GoblinDiggingTeam());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new CarnivorousPlant());

        int teamIdx = gd.playerBattlefields.get(player1.getId()).indexOf(team);
        harness.activateAbility(player1, teamIdx, 0, null, wall.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Carnivorous Plant");
        harness.assertInGraveyard(player2, "Carnivorous Plant");
        harness.assertNotOnBattlefield(player1, "Goblin Digging Team");
        harness.assertInGraveyard(player1, "Goblin Digging Team");
    }

    @Test
    @DisplayName("Ability cannot target a non-Wall creature")
    void cannotTargetNonWall() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = addCreatureReady(player1, new GoblinDiggingTeam());

        Permanent nonWall = harness.addToBattlefieldAndReturn(player2, new GoblinDiggingTeam());

        int teamIdx = gd.playerBattlefields.get(player1.getId()).indexOf(team);
        assertThatThrownBy(() -> harness.activateAbility(player1, teamIdx, 0, null, nonWall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new GoblinDiggingTeam());
        // Summoning sick by default.

        Permanent wall = harness.addToBattlefieldAndReturn(player2, new CarnivorousPlant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
