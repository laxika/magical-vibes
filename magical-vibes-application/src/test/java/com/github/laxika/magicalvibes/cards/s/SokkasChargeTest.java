package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RecklessCohort;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SokkasCharge.class, RecklessCohort.class, GrizzlyBears.class})
class SokkasChargeTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, your Allies have double strike and lifelink")
    void grantsKeywordsToAlliesDuringYourTurn() {
        harness.addToBattlefield(player1, new SokkasCharge());
        Permanent ownAlly = harness.addToBattlefieldAndReturn(player1, new RecklessCohort());
        Permanent ownNonAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingAlly = harness.addToBattlefieldAndReturn(player2, new RecklessCohort());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, ownAlly, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownAlly, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonAlly, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownNonAlly, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingAlly, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingAlly, Keyword.LIFELINK)).isFalse();

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, ownAlly, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownAlly, Keyword.LIFELINK)).isFalse();
    }
}
