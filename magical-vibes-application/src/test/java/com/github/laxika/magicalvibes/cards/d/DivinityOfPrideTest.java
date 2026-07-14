package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DivinityOfPrideTest extends BaseCardTest {

    @Test
    @DisplayName("Base 4/4 at default 20 life")
    void noBoostAtDefaultLife() {
        harness.addToBattlefield(player1, new DivinityOfPride());

        Permanent divinity = findDivinity();
        assertThat(gqs.getEffectivePower(gd, divinity)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, divinity)).isEqualTo(4);
    }

    @Test
    @DisplayName("Still 4/4 at 24 life")
    void noBoostAt24Life() {
        gd.playerLifeTotals.put(player1.getId(), 24);
        harness.addToBattlefield(player1, new DivinityOfPride());

        Permanent divinity = findDivinity();
        assertThat(gqs.getEffectivePower(gd, divinity)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, divinity)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +4/+4 at exactly 25 life")
    void boostAtExactly25Life() {
        gd.playerLifeTotals.put(player1.getId(), 25);
        harness.addToBattlefield(player1, new DivinityOfPride());

        Permanent divinity = findDivinity();
        assertThat(gqs.getEffectivePower(gd, divinity)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, divinity)).isEqualTo(8);
    }

    @Test
    @DisplayName("Loses boost when life drops below 25")
    void losesBoostWhenLifeDrops() {
        gd.playerLifeTotals.put(player1.getId(), 25);
        harness.addToBattlefield(player1, new DivinityOfPride());

        Permanent divinity = findDivinity();
        assertThat(gqs.getEffectivePower(gd, divinity)).isEqualTo(8);

        gd.playerLifeTotals.put(player1.getId(), 24);
        assertThat(gqs.getEffectivePower(gd, divinity)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, divinity)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's life total doesn't affect the boost")
    void opponentLifeDoesNotCount() {
        gd.playerLifeTotals.put(player2.getId(), 50);
        harness.addToBattlefield(player1, new DivinityOfPride());

        Permanent divinity = findDivinity();
        assertThat(gqs.getEffectivePower(gd, divinity)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, divinity)).isEqualTo(4);
    }

    private Permanent findDivinity() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Divinity of Pride"))
                .findFirst().orElseThrow();
    }
}
