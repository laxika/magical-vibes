package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FangrenPathcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures gain trample, but nonattacking creatures do not")
    void attackingCreaturesGainTrample() {
        Permanent pathcutter = addCreatureReady(player1, new FangrenPathcutter());
        Permanent attackingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattackingCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(pathcutter.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(attackingCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(nonattackingCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Granted trample wears off at end of turn")
    void grantedTrampleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new FangrenPathcutter());
        Permanent attackingCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(attackingCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attackingCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
