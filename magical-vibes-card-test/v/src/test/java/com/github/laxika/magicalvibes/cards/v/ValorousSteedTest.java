package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValorousSteed.class})
class ValorousSteedTest extends BaseCardTest {

    @Test
    @DisplayName("When Valorous Steed enters, it creates a 2/2 Knight token with vigilance")
    void etbCreatesKnightTokenWithVigilance() {
        harness.castFromHand(player1, new ValorousSteed(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Knight");
        assertThat(tokens).hasSize(1);

        Permanent token = tokens.getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Knight creation waits for the enter trigger to resolve")
    void tokenCreationUsesTheStack() {
        harness.castFromHand(player1, new ValorousSteed(), "{4}{W}");
        assertThat(countPermanents(player1, "Knight")).isZero();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Valorous Steed");
        assertThat(countPermanents(player1, "Knight")).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
    }

    @Test
    @DisplayName("A Steed entering without being cast creates a Knight for its controller")
    void noncastEntryCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new ValorousSteed());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Knight")).isZero();
        assertThat(findPermanents(player2, "Knight")).hasSize(1);
        Permanent token = findPermanent(player2, "Knight");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.KNIGHT);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Steed and its Knight remain untapped when attacking")
    void vigilanceKeepsBothAttackersUntapped() {
        harness.castFromHand(player1, new ValorousSteed(), "{4}{W}");
        resolveAllTriggers();
        Permanent steed = findPermanent(player1, "Valorous Steed");
        Permanent knight = findPermanent(player1, "Knight");
        steed.setSummoningSick(false);
        knight.setSummoningSick(false);

        declareAttackers(List.of(0, 1));

        assertThat(steed.isTapped()).isFalse();
        assertThat(knight.isTapped()).isFalse();
        harness.assertLife(player2, 15);
    }
}
