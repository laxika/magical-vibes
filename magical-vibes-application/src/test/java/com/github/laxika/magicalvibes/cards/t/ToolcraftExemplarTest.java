package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ToolcraftExemplarTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("With one artifact it gets +2/+1 without first strike")
    void oneArtifactGivesBoost() {
        Permanent exemplar = harness.addToBattlefieldAndReturn(player1, new ToolcraftExemplar());
        harness.addToBattlefield(player1, new Memnite());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(exemplar.getPowerModifier()).isEqualTo(2);
        assertThat(exemplar.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("With three artifacts it also gains first strike")
    void threeArtifactsAlsoGivesFirstStrike() {
        Permanent exemplar = harness.addToBattlefieldAndReturn(player1, new ToolcraftExemplar());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(exemplar.getPowerModifier()).isEqualTo(2);
        assertThat(exemplar.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Without an artifact it does not trigger")
    void noArtifactDoesNotTrigger() {
        Permanent exemplar = harness.addToBattlefieldAndReturn(player1, new ToolcraftExemplar());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(exemplar.getPowerModifier()).isEqualTo(0);
        assertThat(exemplar.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opponent artifacts do not count")
    void opponentArtifactsDoNotCount() {
        Permanent exemplar = harness.addToBattlefieldAndReturn(player1, new ToolcraftExemplar());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(exemplar.getPowerModifier()).isEqualTo(0);
        assertThat(exemplar.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.FIRST_STRIKE)).isFalse();
    }
}
