package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FesteringWound.class, MetathranSoldier.class, BraidwoodCup.class})
class FesteringWoundTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Festering Wound attached to a creature")
    void castsAndAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new MetathranSoldier());

        harness.setHand(player1, List.of(new FesteringWound()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FesteringWound
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot cast Festering Wound attached to a noncreature permanent")
    void cannotAttachToNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BraidwoodCup());

        harness.setHand(player1, List.of(new FesteringWound()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("At the Aura controller's upkeep, they may add an infection counter")
    void controllerMayAddInfectionCounter() {
        Permanent aura = attachWoundToCreature();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(aura.getCounterCount(CounterType.INFECTION)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Aura controller may decline to add an infection counter")
    void controllerMayDeclineInfectionCounter() {
        Permanent aura = attachWoundToCreature();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(aura.getCounterCount(CounterType.INFECTION)).isZero();
    }

    @Test
    @DisplayName("At the enchanted creature controller's upkeep, Festering Wound deals damage equal to its infection counters")
    void damagesEnchantedCreatureControllerForEachInfectionCounter() {
        Permanent aura = attachWoundToCreature();
        aura.setCounterCount(CounterType.INFECTION, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Damage uses the infection counter count when the trigger resolves")
    void damageUsesCountersAtResolution() {
        Permanent aura = attachWoundToCreature();
        aura.setCounterCount(CounterType.INFECTION, 1);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        aura.setCounterCount(CounterType.INFECTION, 4);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Both upkeep abilities trigger when the Aura controller controls the enchanted creature")
    void bothUpkeepAbilitiesTriggerForSameController() {
        Permanent aura = attachWoundToCreature(player1);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(aura.getCounterCount(CounterType.INFECTION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Festering Wound does not deal damage with no infection counters")
    void noDamageWithoutInfectionCounters() {
        attachWoundToCreature();
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private Permanent attachWoundToCreature() {
        return attachWoundToCreature(player2);
    }

    private Permanent attachWoundToCreature(Player creatureController) {
        Permanent creature = addCreatureReady(creatureController, new MetathranSoldier());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FesteringWound());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
