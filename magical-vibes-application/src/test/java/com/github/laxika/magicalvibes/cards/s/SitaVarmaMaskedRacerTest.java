package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Sita Varma, Masked Racer")
class SitaVarmaMaskedRacerTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust puts X counters on Sita and optionally sets other own creatures to her power")
    void exhaustPutsCountersAndSetsOtherCreaturesBasePowerToughness() {
        Permanent sita = addReady(new SitaVarmaMaskedRacer());
        Permanent ownBears = addReady(new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, sita)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sita)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the optional effect leaves other creatures unchanged")
    void mayEffectCanBeDeclined() {
        Permanent sita = addReady(new SitaVarmaMaskedRacer());
        Permanent ownBears = addReady(new GrizzlyBears());
        addManaForX(1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, sita)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The base P/T set wears off at end of turn, but counters remain")
    void basePowerToughnessSetWearsOffAtEndOfTurn() {
        Permanent sita = addReady(new SitaVarmaMaskedRacer());
        Permanent ownBears = addReady(new GrizzlyBears());
        addManaForX(1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(sita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addReady(new SitaVarmaMaskedRacer());
        addExhaustMana();
        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addExhaustMana() {
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void addManaForX(int x) {
        harness.addMana(player1, ManaColor.GREEN, x + 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
