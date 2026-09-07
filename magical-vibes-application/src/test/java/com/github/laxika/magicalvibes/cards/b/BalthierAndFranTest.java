package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalthierAndFran.class, DuskLegionDreadnought.class, GrizzlyBears.class})
class BalthierAndFranTest extends BaseCardTest {

    @Test
    @DisplayName("Balthier and Fran boosts Vehicles and grants them reach and vigilance")
    void boostsVehicles() {
        addCreatureReady(player1, new BalthierAndFran());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A Vehicle crewed by Balthier and Fran can pay for an additional combat phase")
    void crewedVehicleCanPayForAdditionalCombat() {
        Permanent vehicle = crewVehicle();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        declareAttackers(player1, List.of(indexOf(player1, vehicle)), 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("The Vehicle attack trigger does not fire in a later combat phase")
    void doesNotTriggerInLaterCombat() {
        Permanent vehicle = crewVehicle();

        declareAttackers(player1, List.of(indexOf(player1, vehicle)), 2);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Balthier and Fran"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("The trigger only watches the Vehicle that was crewed")
    void doesNotTriggerForAnotherAttacker() {
        Permanent vehicle = crewVehicle();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(indexOf(player1, bear)), 1);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Balthier and Fran"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(vehicle.isTapped()).isFalse();
    }

    private Permanent crewVehicle() {
        addCreatureReady(player1, new BalthierAndFran());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        vehicle.untap();
        return vehicle;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, int combatPhaseNumber) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.combatPhasesThisTurn = combatPhaseNumber;
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
