package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FelhidePetrifierTest extends BaseCardTest {

    @Test
    @DisplayName("Grants deathtouch to other Minotaur creatures you control")
    void grantsDeathtouchToOtherMinotaurs() {
        harness.addToBattlefield(player1, new FelhidePetrifier());
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new FelhideMinotaur());

        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Does not grant deathtouch to non-Minotaur creatures")
    void doesNotGrantDeathtouchToNonMinotaurs() {
        harness.addToBattlefield(player1, new FelhidePetrifier());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not grant deathtouch to an opponent's Minotaur")
    void doesNotGrantDeathtouchToOpponentsMinotaur() {
        harness.addToBattlefield(player1, new FelhidePetrifier());
        Permanent minotaur = harness.addToBattlefieldAndReturn(player2, new FelhideMinotaur());

        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.DEATHTOUCH)).isFalse();
    }
}
