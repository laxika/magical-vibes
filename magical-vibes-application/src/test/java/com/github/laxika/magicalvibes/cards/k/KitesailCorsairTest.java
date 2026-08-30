package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KitesailCorsairTest extends BaseCardTest {

    private Permanent addCorsair() {
        Permanent corsair = addCreatureReady(player1, new KitesailCorsair());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return corsair;
    }

    @Test
    @DisplayName("Does not have flying while not attacking")
    void noFlyingWhileNotAttacking() {
        Permanent corsair = addCorsair();

        assertThat(gqs.hasKeyword(gd, corsair, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has flying while attacking")
    void hasFlyingWhileAttacking() {
        Permanent corsair = addCorsair();

        corsair.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, corsair, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Loses flying when it stops attacking")
    void losesFlyingWhenNotAttacking() {
        Permanent corsair = addCorsair();
        corsair.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, corsair, Keyword.FLYING)).isTrue();

        corsair.setAttacking(false);

        assertThat(gqs.hasKeyword(gd, corsair, Keyword.FLYING)).isFalse();
    }
}
