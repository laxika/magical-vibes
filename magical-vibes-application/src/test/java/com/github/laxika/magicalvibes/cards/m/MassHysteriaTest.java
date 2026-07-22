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

class MassHysteriaTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have haste")
    void ownCreaturesHaveHaste() {
        Permanent bears = addPermanent(player1, new GrizzlyBears());
        addPermanent(player1, new MassHysteria());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures have haste")
    void opponentCreaturesHaveHaste() {
        Permanent opponentBears = addPermanent(player2, new GrizzlyBears());
        addPermanent(player1, new MassHysteria());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste is removed when Mass Hysteria leaves the battlefield")
    void hasteRemovedWhenSourceLeaves() {
        Permanent bears = addPermanent(player1, new GrizzlyBears());
        Permanent opponentBears = addPermanent(player2, new GrizzlyBears());
        addPermanent(player1, new MassHysteria());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Mass Hysteria"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isFalse();
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
