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
