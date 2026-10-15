package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToolcraftExemplar.class, PropheticPrism.class})
class ToolcraftExemplarTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("With one artifact it gets +2/+1 without first strike")
    void oneArtifactGivesBoost() {
        Permanent exemplar = harness.addToBattlefieldAndReturn(player1, new ToolcraftExemplar());
        harness.addToBattlefield(player1, new PropheticPrism());

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
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());

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
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(exemplar.getPowerModifier()).isEqualTo(0);
        assertThat(exemplar.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opponent artifacts do not count")
    void opponentArtifactsDoNotCount() {
        Permanent exemplar = harness.addToBattlefieldAndReturn(player1, new ToolcraftExemplar());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addToBattlefield(player2, new PropheticPrism());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(exemplar.getPowerModifier()).isEqualTo(0);
        assertThat(exemplar.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void losingLastArtifactBeforeResolutionPreventsBoost() {
        Permanent exemplar = harness.addToBattlefieldAndReturn(player1, new ToolcraftExemplar());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact);
        harness.passBothPriorities();

        assertThat(exemplar.getPowerModifier()).isZero();
        assertThat(exemplar.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void reachingThreeArtifactsBeforeResolutionGrantsFirstStrike() {
        Permanent exemplar = harness.addToBattlefieldAndReturn(player1, new ToolcraftExemplar());
        harness.addToBattlefield(player1, new PropheticPrism());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.passBothPriorities();

        assertThat(exemplar.getPowerModifier()).isEqualTo(2);
        assertThat(exemplar.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void droppingToTwoArtifactsBeforeResolutionStillBoostsWithoutFirstStrike() {
        Permanent exemplar = harness.addToBattlefieldAndReturn(player1, new ToolcraftExemplar());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());

        advanceToCombat(player1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact);
        harness.passBothPriorities();

        assertThat(exemplar.getPowerModifier()).isEqualTo(2);
        assertThat(exemplar.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void artifactEnteringAfterCombatBeginsCannotCreateMissedTrigger() {
        Permanent exemplar = harness.addToBattlefieldAndReturn(player1, new ToolcraftExemplar());

        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.passBothPriorities();

        assertThat(exemplar.getPowerModifier()).isZero();
        assertThat(exemplar.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void opponentCombatDoesNotTriggerAbility() {
        Permanent exemplar = harness.addToBattlefieldAndReturn(player1, new ToolcraftExemplar());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(exemplar.getPowerModifier()).isZero();
        assertThat(exemplar.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void resolvedBonusesPersistWithoutArtifactsAndExpireAtEndOfTurn() {
        Permanent exemplar = harness.addToBattlefieldAndReturn(player1, new ToolcraftExemplar());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, third);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(exemplar.getPowerModifier()).isEqualTo(2);
        assertThat(exemplar.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.FIRST_STRIKE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(exemplar.getPowerModifier()).isZero();
        assertThat(exemplar.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.FIRST_STRIKE)).isFalse();
    }
}
