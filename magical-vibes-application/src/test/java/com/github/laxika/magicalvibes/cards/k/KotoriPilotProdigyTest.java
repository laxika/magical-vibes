package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.a.AerialSurveyor;
import com.github.laxika.magicalvibes.cards.e.EtheriumSculptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KotoriPilotProdigy.class, AirResponseUnit.class, GrizzlyBears.class, JhoirasFamiliar.class,
        AerialSurveyor.class, EtheriumSculptor.class})
class KotoriPilotProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Vehicles you control gain crew 2")
    void grantsCrewTwoToVehiclesYouControl() {
        harness.addToBattlefield(player1, new KotoriPilotProdigy());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new AirResponseUnit());
        Permanent pilot = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, vehicle), 1, null, null);
        harness.handlePermanentChosen(player1, pilot.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At the beginning of combat, grants lifelink and vigilance to a target artifact creature you control")
    void grantsLifelinkAndVigilanceAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new KotoriPilotProdigy());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new JhoirasFamiliar());
        Permanent nonartifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(artifactCreature.getId());

        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonartifactCreature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonartifactCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The combat keywords wear off at end of turn")
    void combatKeywordsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new KotoriPilotProdigy());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new JhoirasFamiliar());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetNonartifactCreature() {
        harness.addToBattlefield(player1, new KotoriPilotProdigy());
        harness.addToBattlefieldAndReturn(player1, new JhoirasFamiliar());
        Permanent nonartifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonartifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void combatTargetExcludesOpponentsAndUncrewedVehicles() {
        harness.addToBattlefield(player1, new KotoriPilotProdigy());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new EtheriumSculptor());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new EtheriumSculptor());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new AerialSurveyor());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(ownCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, vehicle.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new KotoriPilotProdigy());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new EtheriumSculptor());

        advanceToCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void crewedVehicleCanReceiveCombatKeywords() {
        Permanent kotori = harness.addToBattlefieldAndReturn(player1, new KotoriPilotProdigy());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new AerialSurveyor());

        harness.activateAbility(player1, battlefieldIndex(player1, vehicle), 1, null, null);
        harness.passBothPriorities();
        assertThat(kotori.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, vehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void crewTwoRequiresAtLeastTwoPower() {
        Permanent kotori = harness.addToBattlefieldAndReturn(player1, new KotoriPilotProdigy());
        kotori.tap();
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new AerialSurveyor());
        Permanent pilot = harness.addToBattlefieldAndReturn(player1, new EtheriumSculptor());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, vehicle), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");

        assertThat(pilot.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
