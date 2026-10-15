package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HightideHermit.class)
class HightideHermitTest extends BaseCardTest {

    @Test
    void entersWithFourEnergyCounters() {
        harness.setHand(player1, List.of(new HightideHermit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void paysEnergyToAttackDespiteDefender() {
        Permanent hermit = addCreatureReady(player1, new HightideHermit());
        harness.addToBattlefield(player2, new HightideHermit());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();

        declareAttackers(List.of(0));

        assertThat(hermit.isAttacking()).isTrue();
    }

    @Test
    void cannotActivateWithoutTwoEnergyCounters() {
        addCreatureReady(player1, new HightideHermit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two energy counters");
    }

    @Test
    void defenderPreventsAttackingWithoutActivation() {
        addCreatureReady(player1, new HightideHermit());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void energyIsPaidImmediatelyAndActivationDoesNotTap() {
        Permanent hermit = addCreatureReady(player1, new HightideHermit());
        gd.playerEnergyCounters.put(player1.getId(), 5);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
        assertThat(hermit.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(hermit.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWithOnlyOneEnergyCounter() {
        addCreatureReady(player1, new HightideHermit());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two energy counters");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackPermissionAppliesOnlyToTheActivatedHermit() {
        Permanent activatedHermit = addCreatureReady(player1, new HightideHermit());
        Permanent otherHermit = addCreatureReady(player1, new HightideHermit());
        harness.addToBattlefield(player2, new HightideHermit());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(otherHermit.isAttacking()).isFalse();

        declareAttackers(List.of(0));

        assertThat(activatedHermit.isAttacking()).isTrue();
    }

    @Test
    void attackPermissionDoesNotBypassSummoningSickness() {
        harness.addToBattlefield(player1, new HightideHermit());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackPermissionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new HightideHermit());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
