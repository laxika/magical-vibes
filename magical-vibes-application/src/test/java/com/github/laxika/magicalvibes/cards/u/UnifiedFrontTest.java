package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnifiedFront.class})
class UnifiedFrontTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one 1/1 white Kor Ally token with one color of mana")
    void createsOneTokenWithOneColor() {
        castWithMana(ManaColor.WHITE, ManaColor.COLORLESS, ManaColor.COLORLESS, ManaColor.COLORLESS);

        assertKorAllyTokens(1);
    }

    @Test
    @DisplayName("Creates one token for each distinct color of mana spent")
    void createsOneTokenForEachDistinctColor() {
        castWithMana(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED);

        assertKorAllyTokens(4);
    }

    @Test
    @DisplayName("Repeated colors and colorless mana do not increase the Converge count")
    void countsEachColorOnce() {
        harness.setHand(player1, List.of(new UnifiedFront()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertKorAllyTokens(1);
    }

    private void castWithMana(ManaColor... manaColors) {
        harness.setHand(player1, List.of(new UnifiedFront()));
        for (ManaColor manaColor : manaColors) {
            harness.addMana(player1, manaColor, 1);
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void assertKorAllyTokens(int count) {
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(count).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes())
                    .containsExactly(CardSubtype.KOR, CardSubtype.ALLY);
        });
    }
}
