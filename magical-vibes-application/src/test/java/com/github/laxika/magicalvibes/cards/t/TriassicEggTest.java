package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TriassicEggTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability puts a hatchling counter on Triassic Egg")
    void putsHatchlingCounterOnEgg() {
        Permanent egg = addReadyEgg();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.HATCHLING)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability requires two hatchling counters")
    void reanimationAbilitiesRequireTwoHatchlingCounters() {
        Permanent egg = addReadyEgg();
        egg.setCounterCount(CounterType.HATCHLING, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The hand mode sacrifices the Egg and may puts a creature from hand onto the battlefield")
    void handModePutsCreatureFromHandOntoBattlefield() {
        Permanent egg = addReadyEgg();
        egg.setCounterCount(CounterType.HATCHLING, 2);
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature, new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Triassic Egg");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The graveyard mode sacrifices the Egg and returns a targeted creature")
    void graveyardModeReturnsTargetCreature() {
        Permanent egg = addReadyEgg();
        egg.setCounterCount(CounterType.HATCHLING, 2);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 2, null, creature.getId(), Zone.GRAVEYARD);
        harness.assertInGraveyard(player1, "Triassic Egg");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The graveyard mode only targets creature cards in your graveyard")
    void graveyardModeRejectsNonCreatureTarget() {
        Permanent egg = addReadyEgg();
        egg.setCounterCount(CounterType.HATCHLING, 2);
        Card land = new Mountain();
        harness.setGraveyard(player1, List.of(land));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 2, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Triassic Egg");
        harness.assertInGraveyard(player1, "Mountain");
    }

    private Permanent addReadyEgg() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new TriassicEgg());
        egg.setSummoningSick(false);
        return egg;
    }
}
