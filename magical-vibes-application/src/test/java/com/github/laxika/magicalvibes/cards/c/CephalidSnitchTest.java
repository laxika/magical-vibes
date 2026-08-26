package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BloodBaronOfVizkopa;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CephalidSnitch.class, BloodBaronOfVizkopa.class, CursedRack.class})
class CephalidSnitchTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Cephalid Snitch removes the target's black protection")
    void removesBlackProtection() {
        harness.addToBattlefield(player1, new CephalidSnitch());
        Permanent baron = harness.addToBattlefieldAndReturn(player2, new BloodBaronOfVizkopa());

        assertThat(gqs.hasProtectionFrom(gd, baron, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, baron, CardColor.WHITE)).isTrue();

        harness.activateAbility(player1, 0, null, baron.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cephalid Snitch");
        assertThat(gqs.hasProtectionFrom(gd, baron, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, baron, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("The target regains black protection at end of turn")
    void protectionReturnsAtEndOfTurn() {
        harness.addToBattlefield(player1, new CephalidSnitch());
        Permanent baron = harness.addToBattlefieldAndReturn(player2, new BloodBaronOfVizkopa());

        harness.activateAbility(player1, 0, null, baron.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasProtectionFrom(gd, baron, CardColor.BLACK)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, baron, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new CephalidSnitch());
        Permanent rack = harness.addToBattlefieldAndReturn(player2, new CursedRack());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rack.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
