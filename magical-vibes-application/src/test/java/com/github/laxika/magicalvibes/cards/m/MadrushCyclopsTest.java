package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MadrushCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain haste")
    void ownCreaturesGainHaste() {
        Permanent bears = addCreature(player1, new GrizzlyBears());
        addCreature(player1, new MadrushCyclops());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Madrush Cyclops himself gains haste")
    void madrushGainsHaste() {
        Permanent madrush = addCreature(player1, new MadrushCyclops());

        assertThat(gqs.hasKeyword(gd, madrush, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain haste")
    void opponentCreaturesDoNotGainHaste() {
        Permanent opponentBears = addCreature(player2, new GrizzlyBears());
        addCreature(player1, new MadrushCyclops());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste is removed when Madrush Cyclops leaves the battlefield")
    void hasteRemovedWhenSourceLeaves() {
        Permanent bears = addCreature(player1, new GrizzlyBears());
        addCreature(player1, new MadrushCyclops());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Madrush Cyclops"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
