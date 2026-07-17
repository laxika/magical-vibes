package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnHavvaConstableTest extends BaseCardTest {

    @Test
    @DisplayName("Alone it counts itself: 2/2")
    void aloneCountsItself() {
        Permanent constable = addConstable(player1);

        assertThat(gqs.getEffectivePower(gd, constable)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each green creature adds one toughness")
    void greenCreaturesAddToughness() {
        Permanent constable = addConstable(player1);
        addCreature(player1, new GrizzlyBears());
        addCreature(player1, new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(4);
    }

    @Test
    @DisplayName("Green creatures on any battlefield count")
    void opponentGreenCreaturesCount() {
        Permanent constable = addConstable(player1);
        addCreature(player2, new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-green creatures do not count")
    void nonGreenCreaturesDontCount() {
        Permanent constable = addConstable(player1);
        addCreature(player1, new HillGiant());

        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(2);
    }

    private Permanent addConstable(Player player) {
        Permanent perm = new Permanent(new AnHavvaConstable());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addCreature(Player player, Card card) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(card));
    }
}
