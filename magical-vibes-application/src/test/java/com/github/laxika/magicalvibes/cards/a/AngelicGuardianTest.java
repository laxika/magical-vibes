package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AngelicGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control gain indestructible")
    void attackingCreaturesYouControlGainIndestructible() {
        Permanent guardian = addCreatureReady(player1, new AngelicGuardian());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new AngelicGuardian());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
