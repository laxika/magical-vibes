package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NectarFaerie;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoggartSpriteChaserTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/2 with no flying when no Faerie is controlled")
    void noBoostWhenNoFaerie() {
        harness.addToBattlefield(player1, new BoggartSpriteChaser());

        Permanent chaser = findPermanent(player1, "Boggart Sprite-Chaser");
        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No boost with a non-Faerie creature")
    void noBoostWithNonFaerie() {
        harness.addToBattlefield(player1, new BoggartSpriteChaser());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent chaser = findPermanent(player1, "Boggart Sprite-Chaser");
        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 and flying when controller controls a Faerie")
    void boostWithFaerie() {
        harness.addToBattlefield(player1, new BoggartSpriteChaser());
        harness.addToBattlefield(player1, new NectarFaerie());

        Permanent chaser = findPermanent(player1, "Boggart Sprite-Chaser");
        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Opponent's Faerie does not grant the bonus")
    void opponentFaerieDoesNotCount() {
        harness.addToBattlefield(player1, new BoggartSpriteChaser());
        harness.addToBattlefield(player2, new NectarFaerie());

        Permanent chaser = findPermanent(player1, "Boggart Sprite-Chaser");
        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses the bonus when the Faerie leaves the battlefield")
    void losesBonusWhenFaerieLeaves() {
        harness.addToBattlefield(player1, new BoggartSpriteChaser());
        harness.addToBattlefield(player1, new NectarFaerie());

        Permanent chaser = findPermanent(player1, "Boggart Sprite-Chaser");
        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Nectar Faerie"));

        assertThat(gqs.getEffectivePower(gd, chaser)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chaser)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.FLYING)).isFalse();
    }
}
