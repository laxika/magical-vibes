package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaprolingBurstTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with seven fade counters")
    void entersWithSevenFadeCounters() {
        harness.setHand(player1, List.of(new SaprolingBurst()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent burst = findPermanent(player1, "Saproling Burst");
        assertThat(burst.getCounterCount(CounterType.FADE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Removing fade counters creates tokens whose size tracks the Burst")
    void createsTokensWithLinkedPowerAndToughness() {
        Permanent burst = addBurstWithFadeCounters(3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(burst.getCounterCount(CounterType.FADE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanents(player1, "Saproling").getFirst())).isEqualTo(2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(burst.getCounterCount(CounterType.FADE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Saproling")).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Fading removes a fade counter at upkeep and sacrifices the Burst at zero")
    void fadingAtUpkeep() {
        Permanent burst = addBurstWithFadeCounters(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(burst.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Saproling Burst");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Saproling Burst");
    }

    @Test
    @DisplayName("The activated ability cannot remove a fade counter when none remain")
    void cannotActivateWithoutFadeCounter() {
        addBurstWithFadeCounters(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Leaving the battlefield destroys tokens created with the Burst")
    void leavingBattlefieldDestroysCreatedTokens() {
        Permanent burst = addBurstWithFadeCounters(2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, burst.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Saproling Burst");
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    private Permanent addBurstWithFadeCounters(int count) {
        Permanent burst = harness.addToBattlefieldAndReturn(player1, new SaprolingBurst());
        burst.setCounterCount(CounterType.FADE, count);
        return burst;
    }
}
