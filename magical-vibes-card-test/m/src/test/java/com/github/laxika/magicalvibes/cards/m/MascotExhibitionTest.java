package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CampusGuide;
import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MascotExhibition.class, EsixFractalBloom.class, CampusGuide.class})
class MascotExhibitionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates the three Mascot Exhibition tokens")
    void createsThreeMascotTokens() {
        harness.setHand(player1, List.of(new MascotExhibition()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

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

    @Test
    @DisplayName("Esix replaces all three tokens from the single creation event")
    void esixReplacesAllThreeTokens() {
        harness.addToBattlefield(player1, new EsixFractalBloom());
        Permanent guide = harness.addToBattlefieldAndReturn(player2, new CampusGuide());
        harness.setHand(player1, List.of(new MascotExhibition()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, guide.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(3)
                .allSatisfy(permanent -> assertThat(permanent.getCard().getName()).isEqualTo("Campus Guide"));
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
