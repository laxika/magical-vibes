package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlindSpotGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SunriseSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("Other Giants you control get +2/+2 and gain trample")
    void buffsOtherGiantsYouControl() {
        harness.addToBattlefield(player1, new BlindSpotGiant());
        Permanent giant = findPermanent(player1, "Blind-Spot Giant");

        int basePower = gqs.getEffectivePower(gd, giant);
        int baseToughness = gqs.getEffectiveToughness(gd, giant);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isFalse();

        harness.addToBattlefield(player1, new SunriseSovereign());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Sunrise Sovereign does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new SunriseSovereign());
        Permanent sovereign = findPermanent(player1, "Sunrise Sovereign");

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, sovereign, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Giant creatures")
    void doesNotBuffNonGiant() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        int basePower = gqs.getEffectivePower(gd, bears);

        harness.addToBattlefield(player1, new SunriseSovereign());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Giants")
    void doesNotBuffOpponentGiants() {
        harness.addToBattlefield(player2, new BlindSpotGiant());
        Permanent giant = findPermanent(player2, "Blind-Spot Giant");

        int basePower = gqs.getEffectivePower(gd, giant);

        harness.addToBattlefield(player1, new SunriseSovereign());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Bonus is removed when Sunrise Sovereign leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new BlindSpotGiant());
        Permanent giant = findPermanent(player1, "Blind-Spot Giant");
        int basePower = gqs.getEffectivePower(gd, giant);

        harness.addToBattlefield(player1, new SunriseSovereign());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(basePower + 2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Sunrise Sovereign"));

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isFalse();
    }
}
