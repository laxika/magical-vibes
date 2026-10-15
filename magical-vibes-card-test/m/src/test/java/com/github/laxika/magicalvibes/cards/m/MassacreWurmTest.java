package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MassacreWurm.class, GrizzlyBears.class, MassOfGhouls.class, Shock.class, Unsummon.class, WrathOfGod.class})
class MassacreWurmTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives -2/-2 to opponent's creatures")
    void etbDebuffsOpponentCreatures() {
        // Opponent has a 5/3 creature that should survive the -2/-2
        harness.addToBattlefield(player2, new MassOfGhouls()); // 5/3

        harness.setHand(player1, List.of(new MassacreWurm()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Massacre Wurm → ETB triggers

        harness.passBothPriorities(); // Resolve ETB

        // Mass of Ghouls should be 3/1 (5-2 / 3-2)
        var ghouls = findPermanent(player2, "Mass of Ghouls");
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
        var bears = findPermanent(player1, "Grizzly Bears");
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
        harness.castAndResolveInstant(player1, 0, bearsId);

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
        harness.castAndResolveInstant(player2, 0, bearsId);

        // Player1's life should be unchanged — death trigger should NOT fire for own creature
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

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

    @Test
    @DisplayName("ETB reduction expires at cleanup and does not affect later arrivals")
    void etbReductionExpiresAndDoesNotAffectLaterArrivals() {
        var ghouls = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());
        harness.setHand(player1, List.of(new MassacreWurm()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        var bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.runStateBasedActions();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(ghouls.getPowerModifier()).isEqualTo(-2);
        assertThat(ghouls.getToughnessModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(ghouls.getPowerModifier()).isZero();
        assertThat(ghouls.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("ETB resolves after Wurm leaves but subsequent deaths do not cause life loss")
    void etbResolvesAfterWurmLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MassacreWurm(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Massacre Wurm"));
        harness.assertInHand(player1, "Massacre Wurm");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Already triggered life loss resolves after Wurm leaves")
    void deathTriggerResolvesAfterWurmLeaves() {
        harness.addToBattlefield(player1, new MassacreWurm());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.assertLife(player2, 20);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Massacre Wurm"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Massacre Wurm");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Wurm sees each opposing creature die simultaneously with itself")
    void simultaneousDeathsTriggerLifeLoss() {
        harness.addToBattlefield(player1, new MassacreWurm());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Massacre Wurm");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning an opposing creature to hand does not trigger life loss")
    void bounceDoesNotTriggerLifeLoss() {
        harness.addToBattlefield(player1, new MassacreWurm());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
