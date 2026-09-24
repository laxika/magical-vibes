package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SylvanOffering.class)
class SylvanOfferingTest extends BaseCardTest {

    @Test
    @DisplayName("With X=2, you and an opponent each create a Treefolk and two Elf Warriors")
    void createsTokensForBothPlayers() {
        cast(2);

        assertTreefolk(player1, 2);
        assertTreefolk(player2, 2);
        assertElfWarriors(player1, 2);
        assertElfWarriors(player2, 2);
    }

    @Test
    @DisplayName("With X=0, no tokens are created")
    void createsNoTokensAtZeroX() {
        cast(0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void cast(int xValue) {
        harness.setHand(player1, List.of(new SylvanOffering()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);

        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }

    private void assertTreefolk(Player player, int xValue) {
        assertThat(findPermanents(player, "Treefolk"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.TREEFOLK);
                    assertThat(token.getEffectivePower()).isEqualTo(xValue);
                    assertThat(token.getEffectiveToughness()).isEqualTo(xValue);
                });
    }

    private void assertElfWarriors(Player player, int count) {
        List<Permanent> tokens = findPermanents(player, "Elf Warrior");
        assertThat(tokens).hasSize(count);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes())
                    .containsExactly(CardSubtype.ELF, CardSubtype.WARRIOR);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }
}
