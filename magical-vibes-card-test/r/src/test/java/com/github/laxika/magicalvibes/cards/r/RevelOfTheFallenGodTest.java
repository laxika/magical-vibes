package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RevelOfTheFallenGod.class})
class RevelOfTheFallenGodTest extends BaseCardTest {

    @Test
    @DisplayName("Creates four 2/2 red and green Satyr tokens with haste")
    void createsFourSatyrTokensWithHaste() {
        harness.setHand(player1, List.of(new RevelOfTheFallenGod()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> tokens = findPermanents(player1, "Satyr");

        assertThat(tokens).hasSize(4);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Satyr"));

        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SATYR);
            assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        }
    }

    @Test
    @DisplayName("All four Satyr tokens can attack the turn they are created")
    void tokensCanAttackImmediately() {
        harness.setHand(player1, List.of(new RevelOfTheFallenGod()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Satyr")).hasSize(4);
        declareAttackers(List.of(0, 1, 2, 3));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }
}
