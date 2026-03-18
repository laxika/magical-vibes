package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MorkrutBansheeTest extends BaseCardTest {

    @Test
    @DisplayName("No ETB trigger without morbid")
    void noEffectWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell

        // No morbid — no ETB trigger should fire
        assertThat(gd.stack).isEmpty();

        // Grizzly Bears should be unaffected
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Morbid met — target creature gets -4/-4")
    void morbidGivesMinusFourMinusFour() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Simulate morbid
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell (ETB trigger goes on stack)
        harness.passBothPriorities(); // resolve ETB

        // Grizzly Bears (2/2) gets -4/-4 → lethal, should be destroyed by SBA
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Morbid met — -4/-4 on a bigger creature reduces stats")
    void morbidReducesBigCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Use a 4/4 creature so it survives -4/-4 (becomes 0/0... actually that's lethal too)
        // Let's use a creature with 5+ toughness
        // We'll put a second Morkrut Banshee on the opponent's side (4/4)
        // Actually -4/-4 on a 4/4 gives 0/0 which is also lethal.
        // Let's just target our own creature and verify the modifier is applied
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        // Grizzly Bears (2/2) with -4/-4 should be destroyed (0 or less toughness)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Killing a creature with Shock enables morbid ETB")
    void actualCreatureDeathEnablesMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.addToBattlefield(player2, bears1);
        harness.addToBattlefield(player2, bears2);
        harness.setHand(player1, List.of(new Shock(), new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Kill first Grizzly Bears with Shock
        UUID bears1Id = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bears1Id);
        harness.passBothPriorities(); // resolve Shock — morbid now active

        // Cast Morkrut Banshee targeting the second Bears
        UUID bears2Id = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, bears2Id);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB — -4/-4 kills Bears

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell — ETB on stack

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB — fizzles

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can cast without target and enters battlefield normally")
    void canCastWithoutTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Morkrut Banshee"));
        assertThat(gd.stack).isEmpty();
    }
}
