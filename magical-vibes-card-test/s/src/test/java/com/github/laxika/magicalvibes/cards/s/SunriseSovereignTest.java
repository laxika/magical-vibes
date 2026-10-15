package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.b.BlindSpotGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunriseSovereign.class, BlindSpotGiant.class, GrizzlyBears.class, AmoeboidChangeling.class})
class SunriseSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("Other Giants you control get +2/+2 and gain trample")
    void buffsOtherGiantsYouControl() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new BlindSpotGiant());

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
        Permanent sovereign = harness.addToBattlefieldAndReturn(player1, new SunriseSovereign());

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, sovereign, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Giant creatures")
    void doesNotBuffNonGiant() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        int basePower = gqs.getEffectivePower(gd, bears);

        harness.addToBattlefield(player1, new SunriseSovereign());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Giants")
    void doesNotBuffOpponentGiants() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new BlindSpotGiant());

        int basePower = gqs.getEffectivePower(gd, giant);

        harness.addToBattlefield(player1, new SunriseSovereign());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Bonus is removed when Sunrise Sovereign leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new BlindSpotGiant());
        int basePower = gqs.getEffectivePower(gd, giant);

        harness.addToBattlefield(player1, new SunriseSovereign());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(basePower + 2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Sunrise Sovereign"));

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Changelings receive the Giant bonus and trample")
    void buffsChangelings() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new AmoeboidChangeling());
        int basePower = gqs.getEffectivePower(gd, changeling);
        int baseToughness = gqs.getEffectiveToughness(gd, changeling);

        harness.addToBattlefield(player1, new SunriseSovereign());

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Multiple Sovereigns buff each other and their bonuses stack on other Giants")
    void multipleSovereignsStack() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new BlindSpotGiant());
        int basePower = gqs.getEffectivePower(gd, giant);
        int baseToughness = gqs.getEffectiveToughness(gd, giant);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SunriseSovereign());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SunriseSovereign());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(basePower + 4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(baseToughness + 4);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isTrue();
        for (Permanent sovereign : java.util.List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(7);
            assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(7);
            assertThat(gqs.hasKeyword(gd, sovereign, Keyword.TRAMPLE)).isTrue();
        }
    }

    @Test
    @DisplayName("Losing the Giant subtype removes both the bonus and trample")
    void losingGiantTypeRemovesBonus() {
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new BlindSpotGiant());
        int basePower = gqs.getEffectivePower(gd, giant);
        int baseToughness = gqs.getEffectiveToughness(gd, giant);
        harness.addToBattlefield(player1, new SunriseSovereign());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(basePower + 2);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isTrue();

        harness.activateAbility(player1, 0, 1, null, giant.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isFalse();
    }
}
