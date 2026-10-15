package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FlamekinBrawler;
import com.github.laxika.magicalvibes.cards.n.NectarFaerie;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoggartSpriteChaser.class, NectarFaerie.class, FlamekinBrawler.class})
class BoggartSpriteChaserTest extends BaseCardTest {

    @Test
    @DisplayName("Gains the bonus immediately when a Faerie enters later")
    void gainsBonusWhenFaerieEnters() {
        Permanent chaser = harness.addToBattlefieldAndReturn(player1, new BoggartSpriteChaser());
        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isFalse();

        harness.addToBattlefield(player1, new NectarFaerie());

        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Multiple Faeries grant only one bonus, which remains until the last leaves")
    void multipleFaeriesDoNotStackBonus() {
        Permanent chaser = harness.addToBattlefieldAndReturn(player1, new BoggartSpriteChaser());
        Permanent firstFaerie = harness.addToBattlefieldAndReturn(player1, new NectarFaerie());
        Permanent secondFaerie = harness.addToBattlefieldAndReturn(player1, new NectarFaerie());

        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstFaerie);

        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(secondFaerie);

        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Base 1/2 with no flying when no Faerie is controlled")
    void noBoostWhenNoFaerie() {
        Permanent chaser = harness.addToBattlefieldAndReturn(player1, new BoggartSpriteChaser());

        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No boost with a non-Faerie creature")
    void noBoostWithNonFaerie() {
        Permanent chaser = harness.addToBattlefieldAndReturn(player1, new BoggartSpriteChaser());
        harness.addToBattlefield(player1, new FlamekinBrawler());

        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 and flying when controller controls a Faerie")
    void boostWithFaerie() {
        Permanent chaser = harness.addToBattlefieldAndReturn(player1, new BoggartSpriteChaser());
        harness.addToBattlefield(player1, new NectarFaerie());

        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Opponent's Faerie does not grant the bonus")
    void opponentFaerieDoesNotCount() {
        Permanent chaser = harness.addToBattlefieldAndReturn(player1, new BoggartSpriteChaser());
        harness.addToBattlefield(player2, new NectarFaerie());

        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses the bonus when the Faerie leaves the battlefield")
    void losesBonusWhenFaerieLeaves() {
        Permanent chaser = harness.addToBattlefieldAndReturn(player1, new BoggartSpriteChaser());
        harness.addToBattlefield(player1, new NectarFaerie());

        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Nectar Faerie"));

        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isFalse();
    }
}
