package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlurryOfHorns.class})
class FlurryOfHornsTest extends BaseCardTest {

    @Test
    void createsTwoHastyMinotaurTokens() {
        harness.castFromHand(player1, new FlurryOfHorns(), "{4}{R}");
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Minotaur");
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(3);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MINOTAUR);
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        });

        harness.assertInGraveyard(player1, "Flurry of Horns");
    }

    @Test
    void bothTokensCanAttackTheTurnTheyEnter() {
        harness.castFromHand(player1, new FlurryOfHorns(), "{4}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Minotaur")).hasSize(2)
                .allSatisfy(token -> assertThat(token.isTapped()).isFalse());

        declareAttackers(List.of(0, 1));
        resolveCombat();

        harness.assertLife(player2, 16);
        assertThat(findPermanents(player1, "Minotaur")).hasSize(2);
    }
}
