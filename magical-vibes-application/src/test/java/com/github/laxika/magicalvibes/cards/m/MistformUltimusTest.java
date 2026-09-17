package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElvishChampion;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        harness.setHand(player1, List.of(mistform));

        assertThat(gqs.cardHasSubtype(mistform, CardSubtype.ELF, gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Mistform Ultimus is every creature type in every non-battlefield zone")
    void isEveryCreatureTypeInEveryNonBattlefieldZone() {
        MistformUltimus handMistform = new MistformUltimus();
        MistformUltimus graveyardMistform = new MistformUltimus();
        MistformUltimus libraryMistform = new MistformUltimus();
        MistformUltimus exileMistform = new MistformUltimus();

        harness.setHand(player1, List.of(handMistform));
        harness.setGraveyard(player1, List.of(graveyardMistform));
        harness.setLibrary(player1, List.of(libraryMistform));
        harness.setExile(player1, List.of(exileMistform));

        assertThat(gqs.cardHasSubtype(handMistform, CardSubtype.WALL, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(graveyardMistform, CardSubtype.WALL, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(libraryMistform, CardSubtype.WALL, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(exileMistform, CardSubtype.WALL, gd, player1.getId())).isTrue();
    }
}
