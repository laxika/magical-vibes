package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MassacreWurmTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB BoostAllCreaturesEffect and ON_OPPONENT_CREATURE_DIES TargetPlayerLosesLifeEffect")
    void hasCorrectStructure() {
        MassacreWurm card = new MassacreWurm();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(BoostAllCreaturesEffect.class);

        BoostAllCreaturesEffect boost = (BoostAllCreaturesEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(-2);
        assertThat(boost.toughnessBoost()).isEqualTo(-2);
        assertThat(boost.filter()).isNotNull();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES).getFirst())
                .isInstanceOf(TargetPlayerLosesLifeEffect.class);

        TargetPlayerLosesLifeEffect lifeLoss = (TargetPlayerLosesLifeEffect) card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES).getFirst();
        assertThat(lifeLoss.amount()).isEqualTo(2);
    }

    // ===== ETB: opponents' creatures get -2/-2 =====

    @Test
    @DisplayName("ETB gives -2/-2 to opponent's creatures")
    void etbDebuffsOpponentCreatures() {
        // Opponent has a 5/5 creature that should survive the -2/-2
        harness.addToBattlefield(player2, new MassOfGhouls()); // 5/3

        harness.setHand(player1, List.of(new MassacreWurm()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Massacre Wurm → ETB triggers

        harness.passBothPriorities(); // Resolve ETB

        // Mass of Ghouls should be 3/1 (5-2 / 3-2)
        var opponent2Battlefield = gd.playerBattlefields.get(player2.getId());
        var ghouls = opponent2Battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Mass of Ghouls"))
                .findFirst().orElseThrow();
        assertThat(ghouls.getPowerModifier()).isEqualTo(-2);
        assertThat(ghouls.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("ETB does not affect controller's own creatures")
    void etbDoesNotAffectOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new MassacreWurm()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Massacre Wurm → ETB triggers

        harness.passBothPriorities(); // Resolve ETB

        // Grizzly Bears should still be unmodified
        var player1Battlefield = gd.playerBattlefields.get(player1.getId());
        var bears = player1Battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB kills opponent's creatures with toughness 2 or less")
    void etbKillsSmallOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 → becomes 0/0 → dies

        harness.setHand(player1, List.of(new MassacreWurm()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Massacre Wurm → ETB triggers

        harness.passBothPriorities(); // Resolve ETB → SBA kills bears

        // Grizzly Bears should be dead
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    // ===== Death trigger: opponent loses 2 life =====

    @Test
    @DisplayName("Opponent loses 2 life when their creature dies")
    void opponentLosesLifeWhenTheirCreatureDies() {
        harness.addToBattlefield(player1, new MassacreWurm());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        harness.setLife(player2, 20);

        // Kill opponent's creature with Shock (2 damage to 2/2)
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger

        harness.passBothPriorities(); // Resolve death trigger

        harness.assertLife(player2, 18); // Lost 2 life
    }

    @Test
    @DisplayName("Death trigger does not fire when controller's own creature dies")
    void deathTriggerDoesNotFireForOwnCreatures() {
        harness.addToBattlefield(player1, new MassacreWurm());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        // Player2 kills player1's creature
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → player1's bears die

        // Player1's life should be unchanged — death trigger should NOT fire for own creature
        harness.assertLife(player1, 20);
    }

    // ===== ETB + death trigger combo =====

    @Test
    @DisplayName("ETB killing opponent's creatures triggers life loss for each")
    void etbKillingCreaturesTriggersLifeLoss() {
        // Opponent has two 2/2 creatures
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new MassacreWurm()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Massacre Wurm → ETB triggers

        harness.passBothPriorities(); // Resolve ETB → both bears die → two death triggers

        // Resolve both death triggers
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Both creatures dead
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Opponent lost 2 life per creature = 4 total
        harness.assertLife(player2, 16);
    }

    // ===== Helper methods =====

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
