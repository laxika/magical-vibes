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

class ElementalMasterpieceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 4/4 blue and red Elemental tokens")
    void createsElementalTokens() {
        harness.setHand(player1, List.of(new ElementalMasterpiece()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Elemental");
            assertThat(token.getEffectivePower()).isEqualTo(4);
            assertThat(token.getEffectiveToughness()).isEqualTo(4);
            assertThat(token.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        });
    }

    @Test
    @DisplayName("The hand ability pays two hybrid mana, discards the source, and creates a Treasure")
    void handAbilityCreatesTreasure() {
        harness.setHand(player1, List.of(new ElementalMasterpiece()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
        harness.assertInGraveyard(player1, "Elemental Masterpiece");
    }
}
