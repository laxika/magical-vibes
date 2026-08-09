package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AggressiveMammothTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have trample")
    void grantsTrampleToOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new AggressiveMammoth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Aggressive Mammoth does not grant trample to an opponent's creature")
    void doesNotGrantTrampleToOpponentsCreature() {
        harness.addToBattlefield(player1, new AggressiveMammoth());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }
}
