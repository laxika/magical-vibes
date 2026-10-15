package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RalsReinforcements.class})
class RalsReinforcementsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Ral's Reinforcements creates two 1/1 blue and red Elemental tokens")
    void createsTwoElementalTokens() {
        harness.setHand(player1, List.of(new RalsReinforcements()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of());

        List<Permanent> tokens = findPermanents(player1, "Elemental");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
            assertThat(token.getCard().isToken()).isTrue();
        });
    }

    @Test
    @DisplayName("Tokens appear only on resolution and enter untapped under the caster's control")
    void createsTokensOnlyWhenSpellResolves() {
        harness.setHand(player1, List.of(new RalsReinforcements()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of());

        assertThat(findPermanents(player1, "Elemental")).isEmpty();
        assertThat(findPermanents(player2, "Elemental")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isSummoningSick()).isTrue();
        });
        assertThat(findPermanents(player2, "Elemental")).isEmpty();
    }
}
