package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MascotExhibitionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates the three Mascot Exhibition tokens")
    void createsThreeMascotTokens() {
        harness.setHand(player1, List.of(new MascotExhibition()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3);

        assertToken(tokens, "Inkling", 2, 1,
                Set.of(CardColor.WHITE, CardColor.BLACK), CardSubtype.INKLING, true);
        assertToken(tokens, "Spirit", 3, 2,
                Set.of(CardColor.RED, CardColor.WHITE), CardSubtype.SPIRIT, false);
        assertToken(tokens, "Elemental", 4, 4,
                Set.of(CardColor.BLUE, CardColor.RED), CardSubtype.ELEMENTAL, false);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void assertToken(List<Permanent> tokens, String name, int power, int toughness,
                             Set<CardColor> colors, CardSubtype subtype, boolean flying) {
        Permanent token = tokens.stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();

        assertThat(token.getEffectivePower()).isEqualTo(power);
        assertThat(token.getEffectiveToughness()).isEqualTo(toughness);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrderElementsOf(colors);
        assertThat(token.getCard().getSubtypes()).contains(subtype);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isEqualTo(flying);
    }
}
