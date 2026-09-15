package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rouse.class, FreshVolunteers.class, Swamp.class})
class RouseTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+0 until end of turn")
    void boostsTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        castForMana(creature.getId());

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        castForMana(creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be cast for 2 life while controlling a Swamp")
    void castsForAlternateCost() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Rouse()));

        harness.castWithAlternateCost(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Alternate cost requires control of a Swamp")
    void alternateCostRequiresSwamp() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.setHand(player1, List.of(new Rouse()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Alternate cost requires a Swamp controlled by the caster")
    void alternateCostRequiresControllerSwamp() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.setHand(player1, List.of(new Rouse()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost requires at least 2 life")
    void alternateCostRequiresEnoughLife() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.setLife(player1, 1);
        harness.setHand(player1, List.of(new Rouse()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The normal mana cost remains available while controlling a Swamp")
    void normalCostRemainsAvailable() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.setLife(player1, 20);
        castForMana(creature.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(creature.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FreshVolunteers());
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Swamp()).getId();
        harness.setHand(player1, List.of(new Rouse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castForMana(UUID targetId) {
        harness.setHand(player1, List.of(new Rouse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
