package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntimidationTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have fear")
    void ownCreaturesHaveFear() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Intimidation());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Creatures you do not control do not gain fear")
    void opponentCreaturesDoNotHaveFear() {
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Intimidation());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Fear is lost when Intimidation leaves the battlefield")
    void fearIsLostWhenSourceLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Intimidation());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Intimidation"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isFalse();
    }
}
