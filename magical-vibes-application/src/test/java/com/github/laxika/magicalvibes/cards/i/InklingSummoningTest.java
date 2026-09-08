package com.github.laxika.magicalvibes.cards.i;

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

class InklingSummoningTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 2/1 white and black Inkling token with flying")
    void createsFlyingInklingToken() {
        harness.setHand(player1, List.of(new InklingSummoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent inkling = findPermanent(player1, "Inkling");
        assertThat(inkling.getCard().getPower()).isEqualTo(2);
        assertThat(inkling.getCard().getToughness()).isEqualTo(1);
        assertThat(inkling.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(inkling.getCard().getSubtypes()).contains(CardSubtype.INKLING);
        assertThat(inkling.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(inkling.getCard().isToken()).isTrue();
    }
}
