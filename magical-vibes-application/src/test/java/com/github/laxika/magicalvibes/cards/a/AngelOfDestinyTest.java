package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfDestiny.class, PlatinumAngel.class})
class AngelOfDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes both the controller and damaged player gain that much life")
    void combatDamageMakesBothPlayersGainThatMuchLife() {
        addCreatureReady(player1, new AngelOfDestiny());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("At the end step, a player attacked by Angel of Destiny loses when life is high enough")
    void attackedPlayerLosesAtEndStepWhenLifeThresholdIsMet() {
        addCreatureReady(player1, new AngelOfDestiny());
        harness.setLife(player1, 35);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The end-step loss does not trigger below the life threshold")
    void endStepLossDoesNotTriggerBelowLifeThreshold() {
        addCreatureReady(player1, new AngelOfDestiny());
        harness.setLife(player1, 30);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The end-step loss is prevented by Platinum Angel")
    void cantLoseEffectPreventsEndStepLoss() {
        addCreatureReady(player1, new AngelOfDestiny());
        addCreatureReady(player2, new PlatinumAngel());
        harness.setLife(player1, 35);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void anotherCreatureTriggersEachAngelsLifeGain() {
        addCreatureReady(player1, new AngelOfDestiny());
        addCreatureReady(player1, new AngelOfDestiny());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(28);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

    @Test
    void blockedAngelStillCausesAttackedPlayerToLose() {
        addCreatureReady(player1, new AngelOfDestiny());
        addCreatureReady(player2, new AngelOfDestiny());
        harness.setLife(player1, 35);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(35);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void nonattackingAngelDoesNotCausePlayerToLose() {
        addCreatureReady(player1, new AngelOfDestiny());
        harness.setLife(player1, 35);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void lifeThresholdIsRecheckedWhenEndStepAbilityResolves() {
        addCreatureReady(player1, new AngelOfDestiny());
        harness.setLife(player1, 35);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passUntil(TurnStep.END_STEP));
        assertThat(gd.stack).isNotEmpty();
        harness.setLife(player1, 34);
        resolveAllTriggers();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void gainingLifeAfterEndStepBeginsDoesNotCreateLossTrigger() {
        addCreatureReady(player1, new AngelOfDestiny());
        harness.setLife(player1, 30);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passUntil(TurnStep.END_STEP));
        assertThat(gd.stack).isEmpty();
        harness.setLife(player1, 35);
        resolveAllTriggers();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void commanderRequiresFifteenLifeAboveForty() {
        gd.format = DeckFormat.COMMANDER;
        addCreatureReady(player1, new AngelOfDestiny());
        harness.setLife(player1, 40);
        harness.setLife(player2, 40);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
