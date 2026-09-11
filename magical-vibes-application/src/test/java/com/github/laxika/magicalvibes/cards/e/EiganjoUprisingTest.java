package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EiganjoUprising.class)
class EiganjoUprisingTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X vigilant Samurai for you and X minus one for each opponent")
    void createsSamuraiForControllerAndOpponents() {
        castUprising(3);

        List<Permanent> ownTokens = findPermanents(player1, "Samurai");
        List<Permanent> opponentTokens = findPermanents(player2, "Samurai");
        assertThat(ownTokens).hasSize(3);
        assertThat(opponentTokens).hasSize(2);
        assertThat(ownTokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        });
        assertThat(opponentTokens).allSatisfy(token -> {
            assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isFalse();
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
        });

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownTokens).allSatisfy(token -> {
            assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isFalse();
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
        });
    }

    @Test
    @DisplayName("Creates no tokens when X is zero")
    void createsNoTokensAtZeroX() {
        castUprising(0);

        assertThat(findPermanents(player1, "Samurai")).isEmpty();
        assertThat(findPermanents(player2, "Samurai")).isEmpty();
    }

    private void castUprising(int xValue) {
        harness.setHand(player1, List.of(new EiganjoUprising()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);

        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }
}
