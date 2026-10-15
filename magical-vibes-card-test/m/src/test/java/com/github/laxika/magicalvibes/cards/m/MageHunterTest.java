package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MageHunter.class, BarkshellBlessing.class, GiantGrowth.class, GrizzlyBears.class,
        MageHuntersOnslaught.class, WitchbaneOrb.class})
class MageHunterTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting an instant causes them to lose 1 life")
    void opponentCastsInstant() {
        harness.addToBattlefield(player1, new MageHunter());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("An opponent copying an instant causes them to lose 1 life")
    void opponentCopiesInstant() {
        harness.addToBattlefield(player1, new MageHunter());
        Permanent conspireA = addCreatureReady(player2, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player2, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BarkshellBlessing()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.castWithConspire(player2, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("The controller's instant does not trigger Mage Hunter")
    void controllerCastsInstant() {
        harness.addToBattlefield(player1, new MageHunter());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("An opponent's sorcery triggers life loss even when it destroys Mage Hunter")
    void opponentSorceryDestroysHunter() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new MageHunter());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MageHuntersOnslaught()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player2, 0, hunter.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Mage Hunter");
    }

    @Test
    @DisplayName("An opponent casting a creature does not trigger Mage Hunter")
    void opponentCastsCreature() {
        harness.addToBattlefield(player1, new MageHunter());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MageHunter()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.setLife(player2, 20);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Mage Hunter");
    }

    @Test
    @DisplayName("The controller's sorcery does not trigger Mage Hunter")
    void controllerCastsSorcery() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MageHunter());
        harness.setHand(player1, List.of(new MageHuntersOnslaught()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each Mage Hunter triggers separately for an opponent's sorcery")
    void multipleHuntersTrigger() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new MageHunter());
        harness.addToBattlefield(player1, new MageHunter());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MageHuntersOnslaught()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player2, 0, hunter.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Hexproof does not stop Mage Hunter's non-targeting cast trigger")
    void opponentWithHexproofStillLosesLife() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new MageHunter());
        harness.addToBattlefield(player2, new WitchbaneOrb());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MageHuntersOnslaught()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player2, 0, hunter.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The controller casting and copying an instant does not trigger Mage Hunter")
    void controllerCopiesInstant() {
        harness.addToBattlefield(player1, new MageHunter());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.castWithConspire(player1, 0, conspireA.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
