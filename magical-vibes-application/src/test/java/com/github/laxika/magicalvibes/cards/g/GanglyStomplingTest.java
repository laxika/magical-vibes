package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GanglyStomplingTest extends BaseCardTest {

    @Test
    @DisplayName("Gangly Stompling is every creature type and has trample")
    void changelingAndTrample() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new GanglyStompling());

        Permanent stompling = findPermanent(player1, "Gangly Stompling");

        assertThat(gqs.hasKeyword(gd, stompling, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, stompling, Keyword.TRAMPLE)).isTrue();
    }
}
