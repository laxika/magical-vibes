package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightWatch.class})
class KnightWatchTest extends BaseCardTest {

    @Test
    void resolvingCreatesTwoVigilantKnightTokensUnderControllerControl() {
        harness.castFromHand(player1, new KnightWatch(), "{4}{W}");
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Knight"))
                .toList();

        assertThat(tokens).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();

        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.KNIGHT);
            assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
        }
    }

    @Test
    void tokensAreCreatedOnlyWhenSpellResolves() {
        harness.castFromHand(player1, new KnightWatch(), "{4}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).allSatisfy(token -> {
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isAttacking()).isFalse();
        });
        harness.assertInGraveyard(player1, "Knight Watch");
    }

    @Test
    void knightTokensRemainUntappedWhenAttacking() {
        harness.castFromHand(player1, new KnightWatch(), "{4}{W}");
        harness.passBothPriorities();
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId());
        assertThat(tokens).hasSize(2);
        tokens.forEach(token -> token.setSummoningSick(false));

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.isTapped()).isFalse();
        });
    }
}
