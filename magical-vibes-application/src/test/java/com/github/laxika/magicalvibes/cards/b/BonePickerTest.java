package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({BonePicker.class, Shock.class, GrizzlyBears.class})
class BonePickerTest extends BaseCardTest {


    @Test
    @DisplayName("Costs {3} less (castable for {B}) when a creature died this turn")
    void castableForOneBlackWithMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BonePicker()));
        // Only one black mana — enough only with the {3} reduction
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Simulate a creature having died this turn
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bone Picker");
    }


    @Test
    @DisplayName("Cannot be cast for {B} when no creature died this turn")
    void cannotCastForOneBlackWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BonePicker()));
        // Only one black mana — not enough without the reduction ({3}{B})
        harness.addMana(player1, ManaColor.BLACK, 1);

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                harness.castCreature(player1, 0));
    }


    @Test
    @DisplayName("Killing a creature with Shock enables the {3} reduction")
    void actualDeathEnablesReduction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new BonePicker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Shock kills the 2/2, satisfying morbid
        java.util.UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Bone Picker now castable for {B}
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bone Picker");
    }

    @Test
    @DisplayName("Can be cast for the full cost without a creature death")
    void castableForFullCostWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BonePicker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bone Picker");
    }

    @Test
    @DisplayName("A creature death does not remove the black mana requirement")
    void morbidStillRequiresBlackMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BonePicker()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.castCreature(player1, 0));
        harness.assertInHand(player1, "Bone Picker");
    }

    @Test
    @DisplayName("A creature already in the graveyard does not enable the reduction")
    void creatureInGraveyardDoesNotEnableMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BonePicker()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.castCreature(player1, 0));
        harness.assertInHand(player1, "Bone Picker");
    }

    @Test
    @DisplayName("A creature dying under the caster's control enables the reduction")
    void ownCreatureDeathEnablesReduction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new BonePicker()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bone Picker");
    }
}
