package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrakestownForgotten.class, GrizzlyBears.class, Shock.class, Plains.class})
class DrakestownForgottenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each creature card in all graveyards")
    void entersWithCountersForCreatureCardsInAllGraveyards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.castFromHand(player1, new DrakestownForgotten(), "{4}{B}");
        harness.passBothPriorities();

        Permanent forgotten = findPermanent(player1, "Drakestown Forgotten");
        assertThat(forgotten.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(forgotten.getEffectivePower()).isEqualTo(3);
        assertThat(forgotten.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Dies as a 0/0 when no creature cards are in the graveyards")
    void diesWithNoCreatureCardsInGraveyards() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.castFromHand(player1, new DrakestownForgotten(), "{4}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drakestown Forgotten");
        harness.assertInGraveyard(player1, "Drakestown Forgotten");
    }

    @Test
    @DisplayName("Removes a +1/+1 counter to give a target creature -1/-1")
    void removesCounterToGiveTargetCreatureMinusOneMinusOne() {
        Permanent forgotten = addCreatureReady(player1, new DrakestownForgotten());
        forgotten.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addManaForDrakestownAbility();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(forgotten.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        Permanent forgotten = addCreatureReady(player1, new DrakestownForgotten());
        forgotten.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addManaForDrakestownAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forgotten = addCreatureReady(player1, new DrakestownForgotten());
        forgotten.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        addManaForDrakestownAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Spending the last counter kills the source but the ability still resolves")
    void abilityResolvesAfterSpendingLastCounter() {
        Permanent forgotten = addCreatureReady(player1, new DrakestownForgotten());
        forgotten.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addManaForDrakestownAbility();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(forgotten.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Drakestown Forgotten");
        harness.assertInGraveyard(player1, "Drakestown Forgotten");
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The debuff ends at cleanup but the spent counter is not restored")
    void debuffEndsAtCleanup() {
        Permanent forgotten = addCreatureReady(player1, new DrakestownForgotten());
        forgotten.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addManaForDrakestownAbility();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(forgotten.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate while summoning sick and target its controller's creature")
    void canActivateWhileSummoningSickAndTargetOwnCreature() {
        Permanent forgotten = harness.addToBattlefieldAndReturn(player1, new DrakestownForgotten());
        forgotten.setSummoningSick(true);
        forgotten.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addManaForDrakestownAbility();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(forgotten.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(forgotten.isTapped()).isFalse();
    }

    private void addManaForDrakestownAbility() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
