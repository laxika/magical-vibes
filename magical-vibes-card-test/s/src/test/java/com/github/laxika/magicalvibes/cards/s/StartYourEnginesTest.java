package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StartYourEngines.class, DuskLegionDreadnought.class, GrizzlyBears.class})
class StartYourEnginesTest extends BaseCardTest {

    @Test
    @DisplayName("Vehicles and creatures you control get the spell's effects")
    void affectsOwnVehiclesAndCreatures() {
        Permanent ownVehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentVehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());

        cast();

        assertThat(gqs.isCreature(gd, ownVehicle)).isTrue();
        assertThat(ownVehicle.getEffectivePower()).isEqualTo(6);
        assertThat(ownVehicle.getEffectiveToughness()).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, opponentVehicle)).isFalse();
    }

    @Test
    @DisplayName("The affected Vehicles and creatures are fixed when the spell resolves")
    void doesNotAffectPermanentsThatArriveLater() {
        cast();
        Permanent laterVehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        Permanent laterCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.isCreature(gd, laterVehicle)).isFalse();
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        cast();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void alreadyCrewedVehicleGetsTheBoostOnlyOnce() {
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        cast();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(6);
    }

    @Test
    void doesNotBoostOpposingCreaturesOrUntapOwnVehicles() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        vehicle.tap();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        cast();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(6);
        assertThat(vehicle.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    private void cast() {
        harness.setHand(player1, List.of(new StartYourEngines()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
