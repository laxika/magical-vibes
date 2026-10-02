package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnyaMercilessAngel.class})
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

    @Test
    @DisplayName("Controller's low life total does not enable Anya's abilities")
    void doesNotCountControllerLife() {
        Permanent anya = harness.addToBattlefieldAndReturn(player1, new AnyaMercilessAngel());
        harness.setLife(player1, 9);
        harness.setLife(player2, 20);

        assertThat(gqs.getEffectivePower(gd, anya)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, anya)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, anya, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Abilities update immediately as an opponent crosses the life threshold")
    void updatesAsOpponentLosesAndGainsLife() {
        Permanent anya = harness.addToBattlefieldAndReturn(player1, new AnyaMercilessAngel());
        harness.setLife(player2, 10);

        assertThat(gqs.getEffectivePower(gd, anya)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, anya)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, anya, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.setLife(player2, 9);

        assertThat(gqs.getEffectivePower(gd, anya)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, anya)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, anya, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.setLife(player2, 10);

        assertThat(gqs.getEffectivePower(gd, anya)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, anya)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, anya, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Commander opponent at 19 life enables the bonus and indestructible")
    void usesCommanderStartingLife() {
        gd.format = DeckFormat.COMMANDER;
        harness.setLife(player1, 40);
        harness.setLife(player2, 19);
        Permanent anya = harness.addToBattlefieldAndReturn(player1, new AnyaMercilessAngel());

        assertThat(gqs.getEffectivePower(gd, anya)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, anya)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, anya, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Commander opponent at exactly 20 life does not enable either ability")
    void excludesExactlyHalfCommanderStartingLife() {
        gd.format = DeckFormat.COMMANDER;
        harness.setLife(player1, 40);
        harness.setLife(player2, 20);
        Permanent anya = harness.addToBattlefieldAndReturn(player1, new AnyaMercilessAngel());

        assertThat(gqs.getEffectivePower(gd, anya)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, anya)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, anya, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
