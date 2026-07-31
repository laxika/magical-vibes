package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuardianOfTheAgesTest extends BaseCardTest {

    @Test
    @DisplayName("When attacked, loses defender and gains trample")
    void losesDefenderAndGainsTrampleWhenAttacked() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheAges());
        addCreatureReady(player2, new AxegrinderGiant());

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isFalse();

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Defender loss and trample gain last past end of turn")
    void keywordChangePersistsPastEndOfTurn() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheAges());
        addCreatureReady(player2, new AxegrinderGiant());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger again after losing defender")
    void doesNotRetriggerWithoutDefender() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheAges());
        Permanent attacker = addCreatureReady(player2, new AxegrinderGiant());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isFalse();
        assertThat(gd.stack).isEmpty();

        // New combat after the guardian already lost defender — intervening if skips the trigger.
        attacker.untap();
        attacker.setAttacking(false);
        harness.forceActivePlayer(player2);
        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }
}
