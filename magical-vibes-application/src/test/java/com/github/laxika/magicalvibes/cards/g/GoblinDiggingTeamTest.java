package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinDiggingTeamTest extends BaseCardTest {

    @Test
    @DisplayName("Ability destroys target Wall and sacrifices the source")
    void destroysTargetWall() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new GoblinDiggingTeam());
        Permanent team = findPermanent(player1, "Goblin Digging Team");
        team.setSummoningSick(false);

        Permanent wall = addWall(player2);

        int teamIdx = gd.playerBattlefields.get(player1.getId()).indexOf(team);
        harness.activateAbility(player1, teamIdx, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(wall.getId()));
        // Source sacrificed as part of the cost.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(team.getId()));
    }

    @Test
    @DisplayName("Ability cannot target a non-Wall creature")
    void cannotTargetNonWall() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new GoblinDiggingTeam());
        Permanent team = findPermanent(player1, "Goblin Digging Team");
        team.setSummoningSick(false);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        int teamIdx = gd.playerBattlefields.get(player1.getId()).indexOf(team);
        assertThatThrownBy(() -> harness.activateAbility(player1, teamIdx, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new GoblinDiggingTeam());
        // Summoning sick by default.

        Permanent wall = addWall(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addWall(Player player) {
        Card wallCard = new GrizzlyBears();
        wallCard.setSubtypes(List.of(CardSubtype.WALL));
        Permanent perm = new Permanent(wallCard);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
