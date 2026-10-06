package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RunedTerror.class, TitanicGrowth.class, TurnToFrog.class})
class RunedTerrorTest extends BaseCardTest {

    @Test
    void playersTakeBeginningAndMainPhasesSequentially() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();

        harness.getGameService().advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.UNTAP);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    void losingAbilitiesRestoresNormalPhaseProgression() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, terror.getId());

        gs.advanceStep(gd);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    void temporaryBoostSurvivesOtherPlayersCleanup() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.setHand(player2, List.of(new TitanicGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int originalPower = gqs.getEffectivePower(gd, terror);
        harness.castAndResolveInstant(player2, 0, terror.getId());
        assertThat(gqs.getEffectivePower(gd, terror)).isEqualTo(originalPower + 4);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, terror)).isEqualTo(originalPower + 4);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, terror)).isEqualTo(originalPower);
    }

    @Test
    void removalLetsCurrentActivePlayerContinueNormally() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(terror);
        gd.playerHands.get(player1.getId()).add(terror.getCard());

        gs.advanceStep(gd);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }
}
