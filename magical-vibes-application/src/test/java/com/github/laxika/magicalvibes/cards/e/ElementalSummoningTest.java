package com.github.laxika.magicalvibes.cards.e;

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

@CardUsed({ElementalSummoning.class})
class ElementalSummoningTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 4/4 blue and red Elemental token")
    void createsElementalToken() {
        harness.setHand(player1, List.of(new ElementalSummoning()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

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

    @Test
    @DisplayName("Red mana can pay both hybrid symbols without changing the token's colors")
    void createsBlueAndRedTokenWithRedMana() {
        harness.setHand(player1, List.of(new ElementalSummoning()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Elemental Summoning");
    }

    @Test
    @DisplayName("Mixed hybrid payment creates a token only when the spell resolves")
    void createsTokenOnResolutionWithMixedMana() {
        harness.setHand(player1, List.of(new ElementalSummoning()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getEffectivePower()).isEqualTo(4);
            assertThat(token.getEffectiveToughness()).isEqualTo(4);
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
