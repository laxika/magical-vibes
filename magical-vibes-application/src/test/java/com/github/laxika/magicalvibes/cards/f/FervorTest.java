package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FervorTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have haste while Fervor is on the battlefield")
    void ownCreaturesHaveHaste() {
        harness.addToBattlefield(player1, new Fervor());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Fervor does not grant haste to opponents' creatures")
    void opponentCreaturesDoNotHaveHaste() {
        harness.addToBattlefield(player1, new Fervor());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isFalse();
    }
}
