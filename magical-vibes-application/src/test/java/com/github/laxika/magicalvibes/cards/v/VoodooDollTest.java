package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
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

    private void resolveControllerEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
