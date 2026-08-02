package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TriplicateSpiritsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three 1/1 white Spirit tokens with flying")
    void createsThreeFlyingSpiritTokens() {
        harness.setHand(player1, List.of(new TriplicateSpirits()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(3);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(spirit.getEffectivePower()).isEqualTo(1);
            assertThat(spirit.getEffectiveToughness()).isEqualTo(1);
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(spirit.hasKeyword(Keyword.FLYING)).isTrue();
        });
    }
}
