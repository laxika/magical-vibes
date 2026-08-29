package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpectralReservesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Spectral Reserves creates two flying Spirit tokens and gains 2 life")
    void createsSpiritsAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SpectralReserves()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(2);
        for (Permanent spirit : spirits) {
            assertThat(spirit.getCard().getPower()).isEqualTo(1);
            assertThat(spirit.getCard().getToughness()).isEqualTo(1);
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(spirit.hasKeyword(Keyword.FLYING)).isTrue();
        }

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Spectral Reserves");
    }
}
