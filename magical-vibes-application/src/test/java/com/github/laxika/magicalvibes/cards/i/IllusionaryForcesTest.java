package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllusionaryForces.class, BalduvianBears.class})
class IllusionaryForcesTest extends BaseCardTest {

    @Test
    @DisplayName("Illusionary Forces cannot be blocked by a creature without flying")
    void cannotBeBlockedByGroundCreature() {
        Permanent forces = addCreatureReady(player1, new IllusionaryForces());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forces);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during Illusionary Forces' controller's upkeep")
    void cumulativeUpkeepTriggersOnlyDuringControllersUpkeep() {
        Permanent forces = harness.addToBattlefieldAndReturn(player1, new IllusionaryForces());

        advanceToUpkeep(player2);

        assertThat(forces.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forces);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Illusionary Forces")
    void paysCumulativeUpkeep() {
        Permanent forces = harness.addToBattlefieldAndReturn(player1, new IllusionaryForces());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(forces.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forces);
    }

    @Test
    @DisplayName("Cumulative upkeep costs two blue mana on the second upkeep")
    void cumulativeUpkeepCostIncreases() {
        Permanent forces = harness.addToBattlefieldAndReturn(player1, new IllusionaryForces());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(forces.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forces);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Illusionary Forces")
    void declineSacrifices() {
        Permanent forces = harness.addToBattlefieldAndReturn(player1, new IllusionaryForces());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forces);
        harness.assertInGraveyard(player1, "Illusionary Forces");
    }
}
