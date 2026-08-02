package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Fecundity;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuratogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an enchantment gives Auratog +2/+2 until end of turn")
    void sacrificeBoostsAuratog() {
        harness.addToBattlefield(player1, new Auratog());
        harness.addToBattlefield(player1, new Fecundity());

        Permanent auratog = findPermanent(player1, "Auratog");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fecundity");
        assertThat(auratog.getPowerModifier()).isEqualTo(2);
        assertThat(auratog.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new Auratog());
        harness.addToBattlefield(player1, new Fecundity());

        Permanent auratog = findPermanent(player1, "Auratog");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(auratog.getPowerModifier()).isEqualTo(0);
        assertThat(auratog.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate ability without an enchantment to sacrifice")
    void cannotActivateWithoutEnchantment() {
        harness.addToBattlefield(player1, new Auratog());
        harness.addToBattlefield(player1, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: Sacrifice an enchantment");
    }
}
