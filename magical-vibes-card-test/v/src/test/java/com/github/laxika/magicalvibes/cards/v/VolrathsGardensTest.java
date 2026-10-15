package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.HornOfGreed;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolrathsGardens.class, SpinedWurm.class, HornOfGreed.class})
class VolrathsGardensTest extends BaseCardTest {

    @Test
    @DisplayName("A summoning-sick creature can pay the tap cost, which is paid before life is gained")
    void canTapSummoningSickCreatureAsCost() {
        harness.addToBattlefield(player1, new VolrathsGardens());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        creature.setSummoningSick(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(creature.isTapped()).isTrue();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot activate during an opponent's main phase")
    void cannotActivateDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new VolrathsGardens());
        Permanent creature = addCreatureReady(player1, new SpinedWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Gardens can be activated again after resolution with another untapped creature")
    void canActivateAgainAfterResolution() {
        Permanent gardens = harness.addToBattlefieldAndReturn(player1, new VolrathsGardens());
        Permanent firstCreature = addCreatureReady(player1, new SpinedWurm());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(gardens.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping an untapped creature and paying {2} gains 2 life")
    void activatedAbilityGainsLife() {
        harness.addToBattlefield(player1, new VolrathsGardens());
        Permanent creature = addCreatureReady(player1, new SpinedWurm());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without an untapped creature you control")
    void cannotActivateWithoutUntappedCreature() {
        harness.addToBattlefield(player1, new VolrathsGardens());
        Permanent creature = addCreatureReady(player1, new SpinedWurm());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate outside your main phase")
    void cannotActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new VolrathsGardens());
        addCreatureReady(player1, new SpinedWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot activate while another ability is on the stack")
    void cannotActivateWithNonEmptyStack() {
        harness.addToBattlefield(player1, new VolrathsGardens());
        addCreatureReady(player1, new SpinedWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        addCreatureReady(player1, new SpinedWurm());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    @DisplayName("Cannot activate without enough mana for {2}")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new VolrathsGardens());
        Permanent creature = addCreatureReady(player1, new SpinedWurm());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot tap a noncreature permanent to pay the cost")
    void cannotTapNoncreaturePermanent() {
        harness.addToBattlefield(player1, new VolrathsGardens());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HornOfGreed());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot tap a creature an opponent controls to pay the cost")
    void cannotTapOpponentsCreature() {
        harness.addToBattlefield(player1, new VolrathsGardens());
        Permanent creature = addCreatureReady(player2, new SpinedWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
    }
}
