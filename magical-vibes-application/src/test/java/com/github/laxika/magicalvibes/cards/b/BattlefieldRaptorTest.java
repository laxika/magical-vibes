package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BattlefieldRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying and first strike on the battlefield")
    void hasFlyingAndFirstStrike() {
        harness.addToBattlefield(player1, new BattlefieldRaptor());

        Permanent raptor = findPermanent(player1, "Battlefield Raptor");

        assertThat(gqs.hasKeyword(gd, raptor, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, raptor, Keyword.FIRST_STRIKE)).isTrue();
    }
}
