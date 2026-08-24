package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElvishChampion;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistformUltimus.class, ElvishChampion.class})
class MistformUltimusTest extends BaseCardTest {

    @Test
    @DisplayName("Mistform Ultimus is every creature type on the battlefield")
    void isEveryCreatureTypeOnBattlefield() {
        harness.addToBattlefield(player1, new ElvishChampion());
        harness.addToBattlefield(player1, new MistformUltimus());

        Permanent mistform = findPermanent(player1, "Mistform Ultimus");

        assertThat(gqs.hasKeyword(gd, mistform, Keyword.CHANGELING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mistform)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mistform)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mistform Ultimus is every creature type while it is in hand")
    void isEveryCreatureTypeOutsideBattlefield() {
        MistformUltimus mistform = new MistformUltimus();
        harness.setHand(player1, java.util.List.of(mistform));

        assertThat(gqs.cardHasSubtype(mistform, CardSubtype.ELF, gd, player1.getId())).isTrue();
    }
}
