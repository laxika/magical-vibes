package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LightningStormkinTest extends BaseCardTest {

    @Test
    @DisplayName("Lightning Stormkin has flying and haste on the battlefield")
    void hasFlyingAndHaste() {
        harness.addToBattlefield(player1, new LightningStormkin());

        Permanent stormkin = findPermanent(player1, "Lightning Stormkin");

        assertThat(gqs.hasKeyword(gd, stormkin, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, stormkin, Keyword.HASTE)).isTrue();
    }
}
