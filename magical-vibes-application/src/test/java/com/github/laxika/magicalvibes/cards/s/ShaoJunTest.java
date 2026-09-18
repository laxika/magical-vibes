package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShaoJun.class, Spellbook.class, GrizzlyBears.class})
class ShaoJunTest extends BaseCardTest {

    @Test
    @DisplayName("Shao Jun has flying and first strike during its controller's turn")
    void hasLeapStrikeDuringOwnTurn() {
        Permanent shaoJun = addCreatureReady(player1, new ShaoJun());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThat(gqs.hasKeyword(gd, shaoJun, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, shaoJun, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Leap Strike is inactive during an opponent's turn")
    void losesLeapStrikeDuringOpponentsTurn() {
        Permanent shaoJun = addCreatureReady(player1, new ShaoJun());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThat(gqs.hasKeyword(gd, shaoJun, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, shaoJun, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Rope Dart taps two artifacts and damages each opponent")
    void ropeDartTapsArtifactsAndDamagesOpponent() {
        Permanent shaoJun = addCreatureReady(player1, new ShaoJun());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int opponentLife = gd.getLife(player2.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shaoJun), null, null);
        harness.passBothPriorities();

        assertThat(firstArtifact.isTapped()).isTrue();
        assertThat(secondArtifact.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 1);
    }

    @Test
    @DisplayName("Rope Dart requires two untapped artifacts")
    void ropeDartRequiresTwoUntappedArtifacts() {
        Permanent shaoJun = addCreatureReady(player1, new ShaoJun());
        harness.addToBattlefield(player1, new Spellbook());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(shaoJun), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }
}
