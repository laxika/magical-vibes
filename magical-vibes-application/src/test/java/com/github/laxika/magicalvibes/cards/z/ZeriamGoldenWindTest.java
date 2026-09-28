package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZeriamGoldenWind.class, EkunduGriffin.class, GrizzlyBears.class})
class ZeriamGoldenWindTest extends BaseCardTest {

    @Test
    @DisplayName("A Griffin dealing combat damage creates a 2/2 white Griffin token with flying")
    void griffinCombatDamageCreatesToken() {
        addCreatureReady(player1, new ZeriamGoldenWind());
        addCreatureReady(player1, new EkunduGriffin());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Griffin");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GRIFFIN);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Each Griffin that deals combat damage creates its own token")
    void eachGriffinCreatesToken() {
        addCreatureReady(player1, new ZeriamGoldenWind());
        addCreatureReady(player1, new EkunduGriffin());
        addCreatureReady(player1, new EkunduGriffin());

        declareAttackers(List.of(1, 2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Griffin")).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-Griffin dealing combat damage does not trigger Zeriam")
    void nonGriffinDoesNotTrigger() {
        addCreatureReady(player1, new ZeriamGoldenWind());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Griffin")).isZero();
    }
}
