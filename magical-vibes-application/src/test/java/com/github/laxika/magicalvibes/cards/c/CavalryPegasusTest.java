package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CavalryPegasusTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Humans gain flying, but non-Humans and nonattacking Humans do not")
    void attackingHumansGainFlying() {
        addCreatureReady(player1, new CavalryPegasus());
        Permanent attackingHuman = addCreatureReady(player1, new DawnhartDisciple());
        Permanent attackingNonHuman = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattackingHuman = addCreatureReady(player1, new DawnhartDisciple());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(attackingHuman.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(attackingNonHuman.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(nonattackingHuman.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void grantedFlyingWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new CavalryPegasus());
        Permanent attackingHuman = addCreatureReady(player1, new DawnhartDisciple());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(attackingHuman.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attackingHuman.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }
}
