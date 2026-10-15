package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShrewdHatchling.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class})
class ShrewdHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with four -1/-1 counters (6/6 becomes 2/2)")
    void entersWithFourMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShrewdHatchling()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        Permanent hatchling = findHatchling(player1);
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(hatchling.getEffectivePower()).isEqualTo(2);
        assertThat(hatchling.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting a blue spell removes a -1/-1 counter")
    void blueSpellRemovesCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities(); // resolve the removal trigger

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a red spell removes a -1/-1 counter")
    void redSpellRemovesCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities(); // resolve the removal trigger

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a non-blue non-red spell does not remove a counter")
    void greenSpellDoesNotRemoveCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Shrewd Hatchling"));
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Targeted creature can't block Shrewd Hatchling this turn")
    void targetCantBlockHatchling() {
        Permanent hatchling = addReadyHatchling(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getCantBlockIds()).contains(hatchling.getId());

        hatchling.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4})
    void blueRedSpellTriggersBothAbilitiesEvenWhenPaidWithRed(int counters) {
        Permanent hatchling = addReadyHatchling(player1);
        hatchling.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, counters);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShrewdHatchling()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isEqualTo(Math.max(0, counters - 1));
        harness.passBothPriorities();
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isEqualTo(Math.max(0, counters - 2));
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Shrewd Hatchling")).hasSize(2);
        assertThat(findPermanents(player1, "Shrewd Hatchling").get(1)
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    @Test
    void opponentsBlueSpellDoesNotRemoveCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new FugitiveWizard(), "{U}");
        resolveAllTriggers();

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    @Test
    void redActivationAllowsTargetToBlockAnotherAttacker() {
        addReadyHatchling(player1);
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();
        otherAttacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        assertThat(blocker.getBlockingTargets()).contains(1);
    }

    private Permanent addReadyHatchling(Player player) {
        Permanent perm = addCreatureReady(player, new ShrewdHatchling());
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        return perm;
    }

    private Permanent findHatchling(Player player) {
        return findPermanent(player, "Shrewd Hatchling");
    }
}
