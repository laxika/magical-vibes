package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SylvanYetiTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of cards in controller's hand; toughness stays 4")
    void powerEqualsHandSize() {
        Permanent yeti = addYetiReady(player1);
        gd.playerHands.get(player1.getId()).clear();

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, yeti)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power is 0 with an empty hand; toughness stays 4")
    void powerZeroWithEmptyHand() {
        Permanent yeti = addYetiReady(player1);
        gd.playerHands.get(player1.getId()).clear();

        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, yeti)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power updates dynamically as hand size changes")
    void powerUpdatesDynamically() {
        Permanent yeti = addYetiReady(player1);
        gd.playerHands.get(player1.getId()).clear();

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(1);

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power counts only controller's hand, not opponent's")
    void countsOnlyControllerHand() {
        Permanent yeti = addYetiReady(player1);
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(1);
    }

    private Permanent addYetiReady(Player player) {
        Permanent permanent = new Permanent(new SylvanYeti());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
