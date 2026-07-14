package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoggleBanditTest extends BaseCardTest {

    @Test
    @DisplayName("Noggle Bandit cannot be blocked by a creature without defender")
    void cannotBeBlockedByNonDefender() {
        Permanent bandit = new Permanent(new NoggleBandit());
        bandit.setSummoningSick(false);
        bandit.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(bandit);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with defender");
    }

    @Test
    @DisplayName("Noggle Bandit can be blocked by a creature with defender")
    void canBeBlockedByDefender() {
        Permanent bandit = new Permanent(new NoggleBandit());
        bandit.setSummoningSick(false);
        bandit.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(bandit);

        Permanent wall = new Permanent(new AngelicWall());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wall);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }
}
