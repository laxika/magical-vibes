package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultaniMaroSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the total number of cards in all players' hands")
    void ptEqualsTotalHandSize() {
        Permanent multani = addMultaniReady(player1);
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, multani)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power and toughness update when any player's hand changes")
    void ptUpdatesWhenHandsChange() {
        Permanent multani = addMultaniReady(player1);
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(1);

        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectiveToughness(gd, multani)).isEqualTo(2);

        gd.playerHands.get(player1.getId()).clear();
        assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(1);
    }

    private Permanent addMultaniReady(Player player) {
        Permanent permanent = new Permanent(new MultaniMaroSorcerer());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
