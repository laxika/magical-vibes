package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MawOfKozilek.class)
class MawOfKozilekTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/-2 until end of turn")
    void getsBoostUntilEndOfTurn() {
        Permanent maw = addReadyMaw();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(maw.getEffectivePower()).isEqualTo(4);
        assertThat(maw.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(maw.getEffectivePower()).isEqualTo(2);
        assertThat(maw.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Requires colorless mana for its activation")
    void requiresColorlessMana() {
        Permanent maw = addReadyMaw();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(maw.getEffectivePower()).isEqualTo(2);
        assertThat(maw.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Repeated activations accumulate and can kill Maw with negative toughness")
    void repeatedActivationsCanKillMaw() {
        Permanent maw = addReadyMaw();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(maw.getEffectivePower()).isEqualTo(6);
        assertThat(maw.getEffectiveToughness()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Maw of Kozilek");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Maw of Kozilek");
        harness.assertInGraveyard(player1, "Maw of Kozilek");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent maw = harness.addToBattlefieldAndReturn(player1, new MawOfKozilek());
        maw.setSummoningSick(true);
        maw.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(maw.getEffectivePower()).isEqualTo(4);
        assertThat(maw.getEffectiveToughness()).isEqualTo(3);
        assertThat(maw.isTapped()).isTrue();
    }

    private Permanent addReadyMaw() {
        return addCreatureReady(player1, new MawOfKozilek());
    }
}
