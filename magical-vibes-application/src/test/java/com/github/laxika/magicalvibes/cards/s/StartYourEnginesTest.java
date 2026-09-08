package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StartYourEnginesTest extends BaseCardTest {

    @Test
    @DisplayName("Vehicles and creatures you control get the spell's effects")
    void affectsOwnVehiclesAndCreatures() {
        harness.addToBattlefield(player1, new DuskLegionDreadnought());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new DuskLegionDreadnought());
        Permanent ownVehicle = findPermanent(player1, "Dusk Legion Dreadnought");
        Permanent opponentVehicle = findPermanent(player2, "Dusk Legion Dreadnought");

        cast();

        assertThat(gqs.isCreature(gd, ownVehicle)).isTrue();
        assertThat(ownVehicle.getEffectivePower()).isEqualTo(6);
        assertThat(ownVehicle.getEffectiveToughness()).isEqualTo(6);
        assertThat(ownVehicle.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, opponentVehicle)).isFalse();
    }

    @Test
    @DisplayName("The affected Vehicles and creatures are fixed when the spell resolves")
    void doesNotAffectPermanentsThatArriveLater() {
        cast();
        harness.addToBattlefield(player1, new DuskLegionDreadnought());
        Permanent laterCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent laterVehicle = findPermanent(player1, "Dusk Legion Dreadnought");

        assertThat(gqs.isCreature(gd, laterVehicle)).isFalse();
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DuskLegionDreadnought());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent vehicle = findPermanent(player1, "Dusk Legion Dreadnought");

        cast();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private void cast() {
        harness.setHand(player1, List.of(new StartYourEngines()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
