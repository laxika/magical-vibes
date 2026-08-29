package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PowerMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Power Matrix boosts the target and grants three keywords")
    void boostsAndGrantsKeywords() {
        Permanent matrix = addReadyMatrix(player1);
        Permanent target = addCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(matrix.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Power Matrix effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        addReadyMatrix(player1);
        Permanent target = addCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyMatrix(Player player) {
        Permanent matrix = harness.addToBattlefieldAndReturn(player, new PowerMatrix());
        matrix.setSummoningSick(false);
        return matrix;
    }

    private Permanent addCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
