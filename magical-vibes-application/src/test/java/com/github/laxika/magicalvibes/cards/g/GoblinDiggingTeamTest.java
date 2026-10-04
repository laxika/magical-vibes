package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.cards.w.WallOfBone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinDiggingTeam.class, WallOfAir.class, WallOfBone.class})
class GoblinDiggingTeamTest extends BaseCardTest {

    @Test
    @DisplayName("Ability can be activated during the opponent's turn")
    void activatesDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        addCreatureReady(player1, new GoblinDiggingTeam());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfAir());

        harness.activateAbility(player1, 0, 0, null, wall.getId());
        harness.assertInGraveyard(player1, "Goblin Digging Team");
        harness.assertOnBattlefield(player2, "Wall of Air");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wall of Air");
        harness.assertNotOnBattlefield(player2, "Wall of Air");
    }

    @Test
    @DisplayName("Rejecting a non-Wall target does not tap or sacrifice the source")
    void invalidTargetDoesNotPayCosts() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = addCreatureReady(player1, new GoblinDiggingTeam());
        Permanent nonWall = harness.addToBattlefieldAndReturn(player2, new GoblinDiggingTeam());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, nonWall.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(team.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Goblin Digging Team");
        harness.assertNotInGraveyard(player1, "Goblin Digging Team");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Wall can regenerate in response while the source remains sacrificed")
    void wallRegeneratesInResponse() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        addCreatureReady(player1, new GoblinDiggingTeam());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfBone());
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, wall.getId());
        harness.assertInGraveyard(player1, "Goblin Digging Team");
        harness.activateAbility(player2, 0, 0, null, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Wall of Bone");
        harness.assertNotInGraveyard(player2, "Wall of Bone");
        assertThat(wall.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Goblin Digging Team");
        harness.assertInGraveyard(player1, "Goblin Digging Team");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability destroys target Wall and sacrifices the source")
    void destroysTargetWall() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = addCreatureReady(player1, new GoblinDiggingTeam());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfAir());

        int teamIdx = gd.playerBattlefields.get(player1.getId()).indexOf(team);
        harness.activateAbility(player1, teamIdx, 0, null, wall.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Air");
        harness.assertInGraveyard(player2, "Wall of Air");
        harness.assertNotOnBattlefield(player1, "Goblin Digging Team");
        harness.assertInGraveyard(player1, "Goblin Digging Team");
    }

    @Test
    @DisplayName("Ability can destroy a Wall controlled by its controller")
    void destroysOwnWall() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = addCreatureReady(player1, new GoblinDiggingTeam());
        Permanent wall = addCreatureReady(player1, new WallOfAir());

        int teamIdx = gd.playerBattlefields.get(player1.getId()).indexOf(team);
        harness.activateAbility(player1, teamIdx, 0, null, wall.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wall of Air");
        harness.assertInGraveyard(player1, "Wall of Air");
        harness.assertNotOnBattlefield(player1, "Goblin Digging Team");
        harness.assertInGraveyard(player1, "Goblin Digging Team");
    }

    @Test
    @DisplayName("Source is sacrificed even if the target leaves before resolution")
    void sacrificesSourceWhenTargetLeavesBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = addCreatureReady(player1, new GoblinDiggingTeam());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfAir());

        int teamIdx = gd.playerBattlefields.get(player1.getId()).indexOf(team);
        harness.activateAbility(player1, teamIdx, 0, null, wall.getId());
        gd.playerBattlefields.get(player2.getId()).remove(wall);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
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

        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfAir());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot activate when the source is already tapped")
    void cannotActivateWhenTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = addCreatureReady(player1, new GoblinDiggingTeam());
        team.tap();
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfAir());

        int teamIdx = gd.playerBattlefields.get(player1.getId()).indexOf(team);
        assertThatThrownBy(() -> harness.activateAbility(player1, teamIdx, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Goblin Digging Team");
        harness.assertNotInGraveyard(player1, "Goblin Digging Team");
    }

    @Test
    @DisplayName("Sacrifice is paid before the ability resolves")
    void sacrificesSourceAsCostBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent team = addCreatureReady(player1, new GoblinDiggingTeam());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfAir());

        int teamIdx = gd.playerBattlefields.get(player1.getId()).indexOf(team);
        harness.activateAbility(player1, teamIdx, 0, null, wall.getId());

        harness.assertNotOnBattlefield(player1, "Goblin Digging Team");
        harness.assertInGraveyard(player1, "Goblin Digging Team");
        harness.assertOnBattlefield(player2, "Wall of Air");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Air");
        harness.assertInGraveyard(player2, "Wall of Air");
    }
}
