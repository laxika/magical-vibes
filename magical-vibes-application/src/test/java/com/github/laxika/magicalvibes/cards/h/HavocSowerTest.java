package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HavocSower.class)
class HavocSowerTest extends BaseCardTest {

    @Test
    @DisplayName("Havoc Sower gets +2/+1 after paying its colorless activation cost")
    void boostsItself() {
        Permanent sower = addCreatureReady(player1, new HavocSower());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sower.getEffectivePower()).isEqualTo(5);
        assertThat(sower.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Havoc Sower's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent sower = addCreatureReady(player1, new HavocSower());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sower.getEffectivePower()).isEqualTo(3);
        assertThat(sower.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Havoc Sower requires colorless mana for its activation")
    void requiresColorlessMana() {
        Permanent sower = addCreatureReady(player1, new HavocSower());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(sower.getEffectivePower()).isEqualTo(3);
        assertThat(sower.getEffectiveToughness()).isEqualTo(3);
    }
}
