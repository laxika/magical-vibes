package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElementalSummoningTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 4/4 blue and red Elemental token")
    void createsElementalToken() {
        harness.setHand(player1, List.of(new ElementalSummoning()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).singleElement().satisfies(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Elemental");
            assertThat(token.getEffectivePower()).isEqualTo(4);
            assertThat(token.getEffectiveToughness()).isEqualTo(4);
            assertThat(token.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        });
    }
}
