package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SerraSphinx.class)
class SerraSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying and vigilance on the battlefield")
    void hasFlyingAndVigilance() {
        harness.addToBattlefield(player1, new SerraSphinx());

        Permanent sphinx = findPermanent(player1, "Serra Sphinx");

        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.VIGILANCE)).isTrue();
    }
}
