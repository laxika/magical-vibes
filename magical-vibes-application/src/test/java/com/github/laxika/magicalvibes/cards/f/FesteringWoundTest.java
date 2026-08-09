package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FesteringWoundTest extends BaseCardTest {

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
    @DisplayName("Festering Wound does not deal damage with no infection counters")
    void noDamageWithoutInfectionCounters() {
        attachWoundToCreature();
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private Permanent attachWoundToCreature() {
        Permanent creature = addCreatureReady(player2, new HillGiant());
        Permanent aura = new Permanent(new FesteringWound());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
