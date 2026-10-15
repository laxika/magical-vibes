package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CrimsonKobolds;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoodooDoll.class, CrimsonKobolds.class, Disenchant.class})
class VoodooDollTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger puts a pin counter on Voodoo Doll")
    void upkeepTriggerAddsPinCounter() {
        Permanent doll = addDoll(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(doll.getCounterCount(CounterType.PIN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability deals damage equal to pin counters")
    void activatedAbilityDealsDamageEqualToPinCounters() {
        Permanent doll = addDoll(player1);
        doll.setCounterCount(CounterType.PIN, 3);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(doll.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability requires X to equal the number of pin counters")
    void activatedAbilityRequiresXToEqualPinCounters() {
        Permanent doll = addDoll(player1);
        doll.setCounterCount(CounterType.PIN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(doll.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activated ability can target a creature")
    void activatedAbilityCanTargetCreature() {
        Permanent doll = addDoll(player1);
        Permanent kobold = addCreatureReady(player2, new CrimsonKobolds());
        doll.setCounterCount(CounterType.PIN, 3);

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 3, kobold.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Crimson Kobolds");
        harness.assertInGraveyard(player2, "Crimson Kobolds");
        assertThat(doll.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapped Voodoo Doll destroys itself and damages its controller at end step")
    void untappedDollDestroysItselfAndDamagesController() {
        Permanent doll = addDoll(player1);
        doll.setCounterCount(CounterType.PIN, 3);
        harness.setLife(player1, 20);

        resolveControllerEndStep(player1);

        harness.assertNotOnBattlefield(player1, "Voodoo Doll");
        harness.assertInGraveyard(player1, "Voodoo Doll");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Tapped Voodoo Doll does not trigger at end step")
    void tappedDollDoesNotTriggerAtEndStep() {
        Permanent doll = addDoll(player1);
        doll.setCounterCount(CounterType.PIN, 3);
        doll.tap();
        harness.setLife(player1, 20);

        resolveControllerEndStep(player1);

        harness.assertOnBattlefield(player1, "Voodoo Doll");
        harness.assertLife(player1, 20);
    }

    private Permanent addDoll(Player owner) {
        return harness.addToBattlefieldAndReturn(owner, new VoodooDoll());
    }

    @Test
    @DisplayName("Opponent's upkeep does not add a pin counter")
    void opponentsUpkeepDoesNotAddCounter() {
        Permanent doll = addDoll(player1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(doll.getCounterCount(CounterType.PIN)).isZero();
    }

    @Test
    @DisplayName("Opponent's end step does not destroy an untapped Doll")
    void opponentsEndStepDoesNotDestroyDoll() {
        Permanent doll = addDoll(player1);
        doll.setCounterCount(CounterType.PIN, 3);
        harness.setLife(player1, 20);

        resolveControllerEndStep(player2);

        harness.assertOnBattlefield(player1, "Voodoo Doll");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Zero pin counters allow a free activation that still taps the Doll")
    void zeroCountersAllowZeroManaActivation() {
        Permanent doll = addDoll(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(doll.isTapped()).isTrue();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The activation requires twice the number of pin counters in mana")
    void activationRequiresDoubleXMana() {
        Permanent doll = addDoll(player1);
        doll.setCounterCount(CounterType.PIN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(doll.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Damage counts pin counters at resolution rather than using the paid X")
    void activationCountsCountersAtResolution() {
        Permanent doll = addDoll(player1);
        doll.setCounterCount(CounterType.PIN, 3);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 3, player2.getId());
        doll.setCounterCount(CounterType.PIN, 4);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Activating in response to the end-step trigger prevents self-destruction")
    void tappingInResponsePreventsSelfDestruction() {
        Permanent doll = addDoll(player1);
        doll.setCounterCount(CounterType.PIN, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        beginControllerEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Voodoo Doll");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An untapped Doll destroyed in response still deals its end-step damage")
    void endStepDamageUsesLastKnownInformationAfterDestruction() {
        Permanent doll = addDoll(player1);
        doll.setCounterCount(CounterType.PIN, 3);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Disenchant()));

        beginControllerEndStep(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, doll.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Voodoo Doll");
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Activated damage still resolves after the Doll is destroyed")
    void activatedDamageUsesLastKnownInformationAfterDestruction() {
        Permanent doll = addDoll(player1);
        doll.setCounterCount(CounterType.PIN, 3);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.castInstant(player1, 0, doll.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Voodoo Doll");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    private void resolveControllerEndStep(Player activePlayer) {
        beginControllerEndStep(activePlayer);
        harness.passBothPriorities();
    }

    private void beginControllerEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
