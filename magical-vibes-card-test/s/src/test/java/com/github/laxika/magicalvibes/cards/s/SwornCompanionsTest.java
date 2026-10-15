package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwornCompanions.class})
class SwornCompanionsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creates two white 1/1 Soldier tokens with lifelink")
    void resolvingCreatesLifelinkSoldierTokens() {
        harness.setHand(player1, List.of(new SwornCompanions()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);

        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
            assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
        }
    }

    @Test
    @DisplayName("Both Soldiers gain life for their controller when dealing combat damage")
    void soldiersGainLifeFromCombatDamage() {
        harness.setHand(player1, List.of(new SwornCompanions()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.performUntapStep(player1);
        declareAttackers(List.of(0, 1));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The other player creates the tokens under their own control")
    void tokensBelongToTheCaster() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SwornCompanions()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.isTapped()).isFalse();
                    assertThat(token.isSummoningSick()).isTrue();
                    assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
                });
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
