package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

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
}
