package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Cancel;
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

@CardUsed({Wurmcalling.class, Cancel.class})
class WurmcallingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 creates a 3/3 green Wurm token")
    void createsWurmTokensUsingPaidX() {
        harness.setHand(player1, List.of(new Wurmcalling()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 3);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WURM);
            assertThat(token.getEffectivePower()).isEqualTo(3);
            assertThat(token.getEffectiveToughness()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("Paying buyback returns Wurmcalling to its owner's hand")
    void buybackReturnsToHand() {
        harness.setHand(player1, List.of(new Wurmcalling()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithBuyback(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Wurmcalling");
        harness.assertNotInGraveyard(player1, "Wurmcalling");
    }

    @Test
    @DisplayName("Without buyback Wurmcalling goes to the graveyard after creating its token")
    void resolvesWithoutBuyback() {
        harness.setHand(player1, List.of(new Wurmcalling()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(2);
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Wurmcalling");
        harness.assertNotInHand(player1, "Wurmcalling");
    }

    @Test
    @DisplayName("X=0 without buyback creates a token that dies and puts Wurmcalling in the graveyard")
    void zeroWithoutBuyback() {
        harness.setHand(player1, List.of(new Wurmcalling()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Wurmcalling");
        harness.assertNotInHand(player1, "Wurmcalling");
    }

    @Test
    @DisplayName("Buyback preserves the chosen X and allows casting again with a different X")
    void buybackWithNonzeroXCanBeRecast() {
        harness.setHand(player1, List.of(new Wurmcalling()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 3, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null, List.of(), true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(3);
            assertThat(token.getEffectiveToughness()).isEqualTo(3);
        });
        harness.assertInHand(player1, "Wurmcalling");
        harness.assertNotInGraveyard(player1, "Wurmcalling");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getEffectivePower).containsExactlyInAnyOrder(3, 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getEffectiveToughness).containsExactlyInAnyOrder(3, 1);
        harness.assertInGraveyard(player1, "Wurmcalling");
        harness.assertNotInHand(player1, "Wurmcalling");
    }

    @Test
    @DisplayName("A countered Wurmcalling does not create a token or return to hand even with buyback paid")
    void counteredSpellDoesNotBuyback() {
        Wurmcalling spell = new Wurmcalling();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 3, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null, List.of(), true);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Wurmcalling");
        harness.assertNotInHand(player1, "Wurmcalling");
    }
}
