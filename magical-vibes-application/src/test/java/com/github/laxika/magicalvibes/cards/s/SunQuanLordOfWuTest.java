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

class SunQuanLordOfWuTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain horsemanship")
    void ownCreaturesGainHorsemanship() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new SunQuanLordOfWu());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Sun Quan himself gains horsemanship")
    void sunQuanGainsHorsemanship() {
        Permanent sunQuan = addReadyCreature(player1, new SunQuanLordOfWu());

        assertThat(gqs.hasKeyword(gd, sunQuan, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain horsemanship")
    void opponentCreaturesDoNotGainHorsemanship() {
        Permanent opponentBears = addReadyCreature(player2, new GrizzlyBears());
        addReadyCreature(player1, new SunQuanLordOfWu());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HORSEMANSHIP)).isFalse();
    }

    @Test
    @DisplayName("Horsemanship is removed when Sun Quan leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new SunQuanLordOfWu());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HORSEMANSHIP)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Sun Quan, Lord of Wu"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HORSEMANSHIP)).isFalse();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
