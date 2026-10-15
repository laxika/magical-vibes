package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarduHateblade;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChiefOfTheScale.class, GrizzlyBears.class, MarduHateblade.class})
class ChiefOfTheScaleTest extends BaseCardTest {

    @Test
    @DisplayName("Other Warriors you control get +0/+1")
    void boostsOtherWarriorsYouControl() {
        harness.addToBattlefield(player1, new ChiefOfTheScale());
        harness.addToBattlefield(player1, new ChiefOfTheScale());

        assertThat(findPermanents(player1, "Chief of the Scale").stream()
                .mapToInt(permanent -> gqs.getEffectivePower(gd, permanent)))
                .containsExactly(2, 2);
        assertThat(findPermanents(player1, "Chief of the Scale").stream()
                .mapToInt(permanent -> gqs.getEffectiveToughness(gd, permanent)))
                .containsExactly(4, 4);
    }

    @Test
    @DisplayName("Chief of the Scale does not boost itself")
    void doesNotBoostItself() {
        Permanent chief = harness.addToBattlefieldAndReturn(player1, new ChiefOfTheScale());
        assertThat(gqs.getEffectivePower(gd, chief)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chief)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-Warrior creatures and opponents' Warriors are unaffected")
    void onlyBoostsOtherOwnWarriors() {
        harness.addToBattlefield(player1, new ChiefOfTheScale());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentChief = harness.addToBattlefieldAndReturn(player2, new ChiefOfTheScale());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentChief)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentChief)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonuses stack on a Warrior entering after the Chiefs")
    void stacksBonusesOnLaterWarrior() {
        harness.addToBattlefield(player1, new ChiefOfTheScale());
        harness.addToBattlefield(player1, new ChiefOfTheScale());

        Permanent warrior = harness.enterBattlefieldAndReturn(player1, new MarduHateblade());

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
    }

    @Test
    @DisplayName("A Warrior loses the bonus when Chief of the Scale leaves")
    void bonusEndsWhenChiefLeaves() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new MarduHateblade());
        Permanent chief = harness.enterBattlefieldAndReturn(player1, new ChiefOfTheScale());

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(2);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, chief));

        harness.assertInGraveyard(player1, "Chief of the Scale");
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(1);
    }
}
