package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MidnightHaunting.class})
class MidnightHauntingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting and resolving Midnight Haunting creates two Spirit tokens")
    void resolvingCreatesTwoTokens() {
        harness.setHand(player1, List.of(new MidnightHaunting()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spirit"))
                .toList();
        assertThat(tokens).hasSize(2);
    }

    @Test
    @DisplayName("Created tokens are 1/1 white Spirits with flying")
    void tokensHaveCorrectStats() {
        harness.setHand(player1, List.of(new MidnightHaunting()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spirit"))
                .toList();

        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    @DisplayName("Midnight Haunting goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new MidnightHaunting()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Midnight Haunting");
    }

    @Test
    @DisplayName("Tokens enter under the controller's control")
    void tokensEnterUnderControllerControl() {
        harness.setHand(player1, List.of(new MidnightHaunting()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spirit"))
                .count()).isEqualTo(2);

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count()).isZero();
    }

    @Test
    @DisplayName("The nonactive player can create untapped Spirits at instant speed")
    void nonactivePlayerCreatesTokensUnderTheirControl() {
        harness.setHand(player2, List.of(new MidnightHaunting()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isSummoningSick()).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        });
        harness.assertInGraveyard(player2, "Midnight Haunting");
    }
}
