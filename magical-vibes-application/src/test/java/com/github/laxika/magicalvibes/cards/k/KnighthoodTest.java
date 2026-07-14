package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KnighthoodTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain first strike")
    void ownCreaturesGainFirstStrike() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Knighthood());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain first strike")
    void opponentCreaturesDoNotGainFirstStrike() {
        Permanent opponentBears = addReadyCreature(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Knighthood());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike is removed when Knighthood leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Knighthood());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Knighthood"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
