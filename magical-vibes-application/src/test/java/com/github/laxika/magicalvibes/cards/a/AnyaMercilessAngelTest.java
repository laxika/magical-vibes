package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AnyaMercilessAngel.class)
class AnyaMercilessAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+3 and indestructible for an opponent below half starting life")
    void getsBonusWhenOpponentIsBelowHalfStartingLife() {
        Permanent anya = addCreatureReady(player1, new AnyaMercilessAngel());
        harness.setLife(player2, 9);

        assertThat(gqs.getEffectivePower(gd, anya)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, anya)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, anya, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Does not count an opponent at exactly half starting life")
    void doesNotGetBonusAtHalfStartingLife() {
        Permanent anya = addCreatureReady(player1, new AnyaMercilessAngel());
        harness.setLife(player2, 10);

        assertThat(gqs.getEffectivePower(gd, anya)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, anya)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, anya, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
