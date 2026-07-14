package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Nightmare;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrimalcruxTest extends BaseCardTest {

    @Test
    @DisplayName("Alone, Primalcrux is 6/6 from its own {G}{G}{G}{G}{G}{G} cost")
    void countsItsOwnGreenPips() {
        Permanent perm = addPrimalcruxReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(6);
    }

    @Test
    @DisplayName("Green pips on other permanents you control are added (6 + 1 = 7)")
    void addsGreenPipsOfOtherOwnPermanents() {
        Permanent perm = addPrimalcruxReady(player1);
        addPermanent(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(7);
    }

    @Test
    @DisplayName("Permanents without green mana symbols contribute nothing")
    void ignoresNonGreenPermanents() {
        Permanent perm = addPrimalcruxReady(player1);
        addPermanent(player1, new Nightmare());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(6);
    }

    @Test
    @DisplayName("Only permanents you control count, not the opponent's")
    void countsOnlyControllerPermanents() {
        Permanent perm = addPrimalcruxReady(player1);
        addPermanent(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(6);
    }

    @Test
    @DisplayName("P/T updates when another green permanent enters")
    void ptUpdatesWhenGreenPermanentAdded() {
        Permanent perm = addPrimalcruxReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(6);

        addPermanent(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(7);
    }

    private Permanent addPrimalcruxReady(Player player) {
        return addPermanent(player, new Primalcrux());
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
