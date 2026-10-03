package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainsCall.class})
class CaptainsCallTest extends BaseCardTest {

    private long soldierTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> "Soldier".equals(p.getCard().getName()))
                .count();
    }

    @Test
    @DisplayName("Resolving Captain's Call creates three 1/1 white Soldier tokens under its controller")
    void createsThreeSoldierTokens() {
        harness.setHand(player1, List.of(new CaptainsCall()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(soldierTokenCount(player1)).isEqualTo(3);
        assertThat(soldierTokenCount(player2)).isZero();

        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .forEach(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                });
    }
    @Test
    @DisplayName("Captain's Call tokens have the specified characteristics and enter untapped")
    void createsWhiteSoldierCreatures() {
        harness.setHand(player1, List.of(new CaptainsCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isSummoningSick()).isTrue();
        });
    }

    @Test
    @DisplayName("Captain's Call creates no tokens before resolution")
    void createsTokensOnlyOnResolution() {
        harness.setHand(player1, List.of(new CaptainsCall()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, List.of());

        assertThat(soldierTokenCount(player1)).isZero();
        assertThat(soldierTokenCount(player2)).isZero();

        harness.passBothPriorities();

        assertThat(soldierTokenCount(player1)).isEqualTo(3);
        assertThat(soldierTokenCount(player2)).isZero();
    }

    @Test
    @DisplayName("Each Captain's Call creates three additional tokens")
    void repeatedCastsCreateSixTokens() {
        harness.setHand(player1, List.of(new CaptainsCall(), new CaptainsCall()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(soldierTokenCount(player1)).isEqualTo(6);
        assertThat(soldierTokenCount(player2)).isZero();
    }
}
