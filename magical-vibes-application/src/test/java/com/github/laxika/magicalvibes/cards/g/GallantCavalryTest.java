package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GallantCavalry.class, Murder.class})
class GallantCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 2/2 white Knight token with vigilance")
    void entersCreatesKnightToken() {
        harness.castFromHand(player1, new GallantCavalry(), "{3}{W}");
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.KNIGHT);
        assertThat(token.getCard().getKeywords()).contains(Keyword.VIGILANCE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("The triggered ability creates its token even after Gallant Cavalry is destroyed")
    void createsTokenAfterSourceIsDestroyed() {
        harness.castFromHand(player1, new GallantCavalry(), "{3}{W}");
        harness.passBothPriorities();

        Permanent cavalry = findPermanent(player1, "Gallant Cavalry");
        assertThat(countPermanents(player1, "Knight")).isZero();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, cavalry.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gallant Cavalry");
        assertThat(countPermanents(player1, "Knight")).isZero();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The opponent's Cavalry creates an untapped white creature for its controller")
    void tokenBelongsToTriggerController() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GallantCavalry(), "{3}{W}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(countPermanents(player2, "Knight")).isEqualTo(1);
        Permanent token = findPermanent(player2, "Knight");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("The created Knight can attack without tapping once summoning sickness ends")
    void tokenVigilanceDoesNotTapWhenAttacking() {
        harness.castFromHand(player1, new GallantCavalry(), "{3}{W}");
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Knight");
        token.setSummoningSick(false);
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        declareAttackersAndPrepareBlockers(List.of(tokenIndex));

        assertThat(token.isAttacking()).isTrue();
        assertThat(token.isTapped()).isFalse();
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
