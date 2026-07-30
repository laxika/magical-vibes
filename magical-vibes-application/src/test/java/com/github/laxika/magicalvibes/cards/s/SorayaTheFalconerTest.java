package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SorayaTheFalconerTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts Bird creatures controlled by either player")
    void boostsBirdsEverywhere() {
        harness.addToBattlefield(player1, new SorayaTheFalconer());
        Permanent ownHawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent enemyHawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, ownHawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownHawk)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, enemyHawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyHawk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost non-Bird creatures")
    void doesNotBoostNonBirds() {
        harness.addToBattlefield(player1, new SorayaTheFalconer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Grants banding to a target Bird until end of turn")
    void grantsBandingToBird() {
        harness.addToBattlefield(player1, new SorayaTheFalconer());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, hawk.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.BANDING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Bird creature")
    void cannotTargetNonBird() {
        harness.addToBattlefield(player1, new SorayaTheFalconer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
