package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionWhite;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Auratog.class, CircleOfProtectionWhite.class, LotusPetal.class})
class AuratogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an enchantment gives Auratog +2/+2 until end of turn")
    void sacrificeBoostsAuratog() {
        Permanent auratog = harness.addToBattlefieldAndReturn(player1, new Auratog());
        harness.addToBattlefield(player1, new CircleOfProtectionWhite());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Circle of Protection: White");
        harness.assertInGraveyard(player1, "Circle of Protection: White");
        assertThat(auratog.getPowerModifier()).isEqualTo(2);
        assertThat(auratog.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent auratog = harness.addToBattlefieldAndReturn(player1, new Auratog());
        harness.addToBattlefield(player1, new CircleOfProtectionWhite());

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
        harness.addToBattlefield(player1, new LotusPetal());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: Sacrifice an enchantment");
        harness.assertOnBattlefield(player1, "Lotus Petal");
    }

    @Test
    @DisplayName("Repeated activations sacrifice one enchantment each and stack the boosts")
    void repeatedActivationsStack() {
        Permanent auratog = harness.addToBattlefieldAndReturn(player1, new Auratog());
        Permanent firstEnchantment = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionWhite());
        harness.addToBattlefield(player1, new CircleOfProtectionWhite());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstEnchantment.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Circle of Protection: White");
        assertThat(auratog.getPowerModifier()).isEqualTo(4);
        assertThat(auratog.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot sacrifice an enchantment controlled by an opponent")
    void cannotSacrificeOpponentsEnchantment() {
        harness.addToBattlefield(player1, new Auratog());
        harness.addToBattlefield(player2, new CircleOfProtectionWhite());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: Sacrifice an enchantment");
        harness.assertOnBattlefield(player2, "Circle of Protection: White");
    }
}
