package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.v.VituGhaziTheCityTree;
import com.github.laxika.magicalvibes.cards.v.VotaryOfTheConclave;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TidewaterMinion.class, VotaryOfTheConclave.class, VituGhaziTheCityTree.class})
class TidewaterMinionTest extends BaseCardTest {

    @Test
    void losesDefenderUntilEndOfTurn() {
        Permanent minion = addCreatureReady(player1, new TidewaterMinion());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(minion.hasKeyword(Keyword.DEFENDER)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(minion.hasKeyword(Keyword.DEFENDER)).isTrue();
    }

    @Test
    void untapsTargetPermanent() {
        Permanent minion = addCreatureReady(player1, new TidewaterMinion());
        Permanent target = addCreatureReady(player2, new VotaryOfTheConclave());
        target.tap();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(minion.isTapped()).isTrue();
    }

    @Test
    void untapsTargetNoncreaturePermanent() {
        Permanent minion = addCreatureReady(player1, new TidewaterMinion());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VituGhaziTheCityTree());
        target.tap();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(minion.isTapped()).isTrue();
    }

    @Test
    void canUntapItself() {
        Permanent minion = addCreatureReady(player1, new TidewaterMinion());

        harness.activateAbility(player1, 0, 1, null, minion.getId());

        assertThat(minion.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(minion.isTapped()).isFalse();
    }

    @Test
    void canTargetAnUntappedPermanent() {
        Permanent minion = addCreatureReady(player1, new TidewaterMinion());
        Permanent target = addCreatureReady(player2, new VotaryOfTheConclave());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(minion.isTapped()).isTrue();
    }

    @Test
    void canLoseDefenderWhileTappedAndSummoningSick() {
        Permanent minion = harness.addToBattlefieldAndReturn(player1, new TidewaterMinion());
        minion.setSummoningSick(true);
        minion.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(minion.hasKeyword(Keyword.DEFENDER)).isTrue();

        harness.passBothPriorities();

        assertThat(minion.hasKeyword(Keyword.DEFENDER)).isFalse();
        assertThat(minion.isTapped()).isTrue();
    }

    @Test
    void cannotPayTapCostWhileSummoningSick() {
        Permanent minion = harness.addToBattlefieldAndReturn(player1, new TidewaterMinion());
        minion.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, minion.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(minion.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
