package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LavaleaperTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures, including Lavaleaper, have haste")
    void allCreaturesHaveHaste() {
        Permanent lavaleaper = addPermanent(player1, new Lavaleaper());
        Permanent bears = addPermanent(player1, new GrizzlyBears());
        Permanent opponentBears = addPermanent(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, lavaleaper, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Tapping a basic land adds one additional mana of the type it produced")
    void addsExtraManaForBasicLand() {
        harness.addToBattlefield(player1, new Lavaleaper());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("The basic-land trigger is symmetric")
    void addsExtraManaForOpponentsBasicLand() {
        harness.addToBattlefield(player1, new Lavaleaper());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping a nonbasic land does not trigger the extra mana")
    void doesNotAddExtraManaForNonbasicLand() {
        harness.addToBattlefield(player1, new Lavaleaper());
        harness.addToBattlefield(player1, new ZhalfirinVoid());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
