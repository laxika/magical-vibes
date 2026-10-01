package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FeudkillersVerdict.class)
class FeudkillersVerdictTest extends BaseCardTest {

    private long giantTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GIANT))
                .count();
    }

    private void cast() {
        harness.castFromHand(player1, new FeudkillersVerdict(), "{4}{W}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gains 10 life and creates a 5/5 Giant when you end up with more life than an opponent")
    void gainsLifeAndCreatesToken() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(30);
        assertThat(giantTokenCount()).isEqualTo(1);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GIANT))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WARRIOR);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, token)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gains 10 life but creates no token when no opponent has less life")
    void gainsLifeButNoTokenWhenNotAhead() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 40);

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(30);
        assertThat(giantTokenCount()).isZero();
    }

    @Test
    @DisplayName("Equal life after gaining is not 'more life than an opponent' — no token")
    void equalLifeCreatesNoToken() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 30);

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(30);
        assertThat(giantTokenCount()).isZero();
    }
}
