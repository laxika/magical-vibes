package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BonePickerTest extends BaseCardTest {

    // ===== With morbid: {3} cheaper =====

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

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bone Picker"));
    }

    // ===== Without morbid: full price =====

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

    // ===== Integration: actual creature death enables the reduction =====

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
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Bone Picker now castable for {B}
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bone Picker"));
    }
}
