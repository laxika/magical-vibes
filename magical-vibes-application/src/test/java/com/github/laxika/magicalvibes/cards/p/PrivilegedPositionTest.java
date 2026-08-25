package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrivilegedPosition.class, FountainOfYouth.class, GrizzlyBears.class})
class PrivilegedPositionTest extends BaseCardTest {

    @Test
    @DisplayName("Other permanents you control have hexproof")
    void grantsHexproofToOtherPermanentsYouControl() {
        harness.addToBattlefield(player1, new PrivilegedPosition());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Privileged Position and opponents' permanents do not gain hexproof")
    void excludesSourceAndOpponentsPermanents() {
        Permanent position = harness.addToBattlefieldAndReturn(player1, new PrivilegedPosition());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, position, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Permanents lose granted hexproof when Privileged Position leaves")
    void removesHexproofWhenSourceLeaves() {
        Permanent position = harness.addToBattlefieldAndReturn(player1, new PrivilegedPosition());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(position);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }
}
