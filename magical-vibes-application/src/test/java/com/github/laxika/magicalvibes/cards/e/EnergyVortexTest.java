package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnergyVortex.class, Boomerang.class})
class EnergyVortexTest extends BaseCardTest {

    @Test
    @DisplayName("{X} ability puts X vortex counters on during the controller's upkeep")
    void abilityPutsXVortexCounters() {
        Permanent vortex = harness.addToBattlefieldAndReturn(player1, new EnergyVortex());

        advanceToUpkeep(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(vortex.getCounterCount(CounterType.VORTEX)).isEqualTo(3);
    }

    @Test
    @DisplayName("{X} ability cannot be activated outside the controller's upkeep")
    void abilityRestrictedToOwnUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new EnergyVortex());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller's upkeep removes all vortex counters")
    void controllerUpkeepRemovesAllCounters() {
        Permanent vortex = harness.addToBattlefieldAndReturn(player1, new EnergyVortex());
        vortex.setCounterCount(CounterType.VORTEX, 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(vortex.getCounterCount(CounterType.VORTEX)).isZero();
    }

    @Test
    @DisplayName("Opponent declining the per-counter payment takes 3 damage")
    void opponentDecliningTakesThreeDamage() {
        Permanent vortex = harness.addToBattlefieldAndReturn(player1, new EnergyVortex());
        vortex.setCounterCount(CounterType.VORTEX, 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent paying {1} per vortex counter avoids the damage")
    void opponentPayingAvoidsDamage() {
        Permanent vortex = harness.addToBattlefieldAndReturn(player1, new EnergyVortex());
        vortex.setCounterCount(CounterType.VORTEX, 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Accepting without enough mana still deals the damage")
    void acceptingWithoutManaStillDealsDamage() {
        Permanent vortex = harness.addToBattlefieldAndReturn(player1, new EnergyVortex());
        vortex.setCounterCount(CounterType.VORTEX, 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("With no vortex counters the opponent is not prompted and takes no damage")
    void noCountersMeansNoPromptAndNoDamage() {
        harness.addToBattlefieldAndReturn(player1, new EnergyVortex());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Upkeep ability still deals damage after Energy Vortex leaves the battlefield")
    void upkeepAbilityUsesLastKnownInformationAfterVortexLeavesBattlefield() {
        Permanent vortex = harness.addToBattlefieldAndReturn(player1, new EnergyVortex());
        vortex.setCounterCount(CounterType.VORTEX, 2);
        harness.setHand(player2, List.of(new Boomerang()));

        advanceToUpkeep(player2);

        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, vortex.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 17);
    }
}
