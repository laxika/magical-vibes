package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CallTheCavalryTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Call the Cavalry has spell effect that creates two 2/2 white Knight tokens with vigilance")
    void hasCorrectProperties() {
        CallTheCavalry card = new CallTheCavalry();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect tokenEffect =
                (CreateTokenEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(tokenEffect.amount()).isEqualTo(2);
        assertThat(tokenEffect.tokenName()).isEqualTo("Knight");
        assertThat(tokenEffect.power()).isEqualTo(2);
        assertThat(tokenEffect.toughness()).isEqualTo(2);
        assertThat(tokenEffect.color()).isEqualTo(CardColor.WHITE);
        assertThat(tokenEffect.subtypes()).containsExactly(CardSubtype.KNIGHT);
        assertThat(tokenEffect.keywords()).containsExactly(Keyword.VIGILANCE);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving creates two 2/2 white Knight tokens with vigilance")
    void resolvingCreatesTwoKnightTokens() {
        harness.setHand(player1, List.of(new CallTheCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Knight"))
                .toList();
        assertThat(tokens).hasSize(2);

        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.KNIGHT);
            assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
        }
    }

    @Test
    @DisplayName("Call the Cavalry goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new CallTheCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Call the Cavalry"));
    }

    @Test
    @DisplayName("Tokens enter under the controller's control")
    void tokensEnterUnderControllerControl() {
        harness.setHand(player1, List.of(new CallTheCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Knight"))
                .count()).isEqualTo(2);

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count()).isZero();
    }
}
