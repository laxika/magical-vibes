package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SoltariLancerTest extends BaseCardTest {

    private Permanent addLancer() {
        Permanent lancer = addCreatureReady(player1, new SoltariLancer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return lancer;
    }

    @Test
    @DisplayName("Does not have first strike while not attacking")
    void noFirstStrikeWhileNotAttacking() {
        Permanent lancer = addLancer();

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has first strike while attacking")
    void hasFirstStrikeWhileAttacking() {
        Permanent lancer = addLancer();

        lancer.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Loses first strike when it stops attacking")
    void losesFirstStrikeWhenNotAttacking() {
        Permanent lancer = addLancer();
        lancer.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();

        lancer.setAttacking(false);

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
    }
}
