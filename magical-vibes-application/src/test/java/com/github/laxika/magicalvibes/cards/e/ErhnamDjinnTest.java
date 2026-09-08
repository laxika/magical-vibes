package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ErhnamDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger grants forestwalk to a non-Wall creature an opponent controls")
    void grantsForestwalkToTarget() {
        harness.addToBattlefield(player1, new ErhnamDjinn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("The upkeep trigger cannot target own creatures or Walls")
    void rejectsOwnCreaturesAndWalls() {
        harness.addToBattlefield(player1, new ErhnamDjinn());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfWood());
        Permanent validTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, wall.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, validTarget.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, validTarget, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, wall, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Granted forestwalk lasts through the turn and ends at the controller's next upkeep")
    void forestwalkEndsAtNextUpkeep() {
        harness.addToBattlefield(player1, new ErhnamDjinn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        advanceToUpkeep(player1);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

}
