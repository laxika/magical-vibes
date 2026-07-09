package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SerrasBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain vigilance")
    void ownCreaturesGainVigilance() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerrasBlessing());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain vigilance")
    void opponentCreaturesDoNotGainVigilance() {
        Permanent opponentBears = addReadyCreature(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerrasBlessing());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance is removed when Serra's Blessing leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerrasBlessing());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Serra's Blessing"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
