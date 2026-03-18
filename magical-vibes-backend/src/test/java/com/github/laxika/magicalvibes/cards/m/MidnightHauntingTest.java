package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MidnightHauntingTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Midnight Haunting has spell effect that creates two 1/1 white Spirit tokens with flying")
    void hasCorrectProperties() {
        MidnightHaunting card = new MidnightHaunting();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(CreateCreatureTokenEffect.class);
        CreateCreatureTokenEffect tokenEffect =
                (CreateCreatureTokenEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(tokenEffect.amount()).isEqualTo(2);
        assertThat(tokenEffect.tokenName()).isEqualTo("Spirit");
        assertThat(tokenEffect.power()).isEqualTo(1);
        assertThat(tokenEffect.toughness()).isEqualTo(1);
        assertThat(tokenEffect.color()).isEqualTo(CardColor.WHITE);
        assertThat(tokenEffect.subtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(tokenEffect.keywords()).containsExactly(Keyword.FLYING);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting and resolving Midnight Haunting creates two Spirit tokens")
    void resolvingCreatesTwoTokens() {
        harness.setHand(player1, List.of(new MidnightHaunting()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spirit"))
                .toList();

        for (Permanent token : tokens) {
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

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Midnight Haunting"));
    }

    @Test
    @DisplayName("Tokens enter under the controller's control")
    void tokensEnterUnderControllerControl() {
        harness.setHand(player1, List.of(new MidnightHaunting()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spirit"))
                .count()).isEqualTo(2);

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count()).isZero();
    }
}
