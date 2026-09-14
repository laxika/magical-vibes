package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NantukoBlightcutter;
import com.github.laxika.magicalvibes.cards.s.StrengthOfIsolation;
import com.github.laxika.magicalvibes.cards.s.StrengthOfLunacy;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CephalidSnitch.class, NantukoBlightcutter.class, StrengthOfLunacy.class,
        StrengthOfIsolation.class, CabalCoffers.class})
class CephalidSnitchTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Cephalid Snitch removes the target's black protection")
    void removesBlackProtection() {
        harness.addToBattlefield(player1, new CephalidSnitch());
        Permanent blightcutter = harness.addToBattlefieldAndReturn(player2, new NantukoBlightcutter());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new StrengthOfLunacy());
        aura.setAttachedTo(blightcutter.getId());

        assertThat(gqs.hasProtectionFrom(gd, blightcutter, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, blightcutter, CardColor.WHITE)).isTrue();

        harness.activateAbility(player1, 0, null, blightcutter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cephalid Snitch");
        assertThat(gqs.hasProtectionFrom(gd, blightcutter, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, blightcutter, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("The target regains black protection at end of turn")
    void protectionReturnsAtEndOfTurn() {
        harness.addToBattlefield(player1, new CephalidSnitch());
        Permanent blightcutter = harness.addToBattlefieldAndReturn(player2, new NantukoBlightcutter());

        harness.activateAbility(player1, 0, null, blightcutter.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasProtectionFrom(gd, blightcutter, CardColor.BLACK)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, blightcutter, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("A later black protection grant overrides the loss until end of turn")
    void laterProtectionGrantRestoresProtection() {
        harness.addToBattlefield(player1, new CephalidSnitch());
        Permanent blightcutter = harness.addToBattlefieldAndReturn(player2, new NantukoBlightcutter());

        harness.activateAbility(player1, 0, null, blightcutter.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasProtectionFrom(gd, blightcutter, CardColor.BLACK)).isFalse();

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new StrengthOfIsolation());
        aura.setAttachedTo(blightcutter.getId());

        assertThat(gqs.hasProtectionFrom(gd, blightcutter, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new CephalidSnitch());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CabalCoffers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
