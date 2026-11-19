package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PersistentSpecimen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.rules.TurnEvent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;

@CardUsed({AlchemistsGambit.class, PersistentSpecimen.class})
class AlchemistsGambitTest extends BaseCardTest {

    /**
     * Empties both opening hands and stocks player 1's library with cards it can't cast: a random land in hand or
     * drawn during the extra turn is a play that stops auto-pass in the postcombat main phase instead of the end
     * step, so whether the delayed loss trigger is reached would change from run to run.
     */
    @BeforeEach
    void fixHandsAndDraws() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new PersistentSpecimen(), new PersistentSpecimen(),
                new PersistentSpecimen(), new PersistentSpecimen(), new PersistentSpecimen()));
    }

    @Test
    @DisplayName("Cast normally, it gives an extra turn with a delayed loss and is exiled")
    void normalCastQueuesExtraTurnAndDelayedLossAndExilesSpell() {
        AlchemistsGambit card = castNormally();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.END_STEP)).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Cast with cleave, it gives an extra turn without the delayed loss and is exiled")
    void cleaveCastQueuesExtraTurnWithoutDelayedLossAndExilesSpell() {
        AlchemistsGambit card = castCleaved();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.END_STEP)).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    private AlchemistsGambit castCleaved() {
        AlchemistsGambit card = new AlchemistsGambit();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        return card;
    }

    @Test
    @DisplayName("Damage can't be prevented during the extra turn")
    void damageCannotBePreventedDuringTheExtraTurn() {
        enableAutoStop();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        attacker.setSummoningSick(false);
        harness.setLife(player2, 20);


        castNormally();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        gd.addPlayerDamagePreventionShield(player2.getId(), 1);
        attacker.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDamagePreventionShields()).containsEntry(player2.getId(), 1);
    }

    @Test
    @DisplayName("Its controller loses only at the end step of the extra turn it gave")
    void lossTriggersOnlyAtTheGrantedTurnsEndStep() {
        enableAutoStop();
        castNormally();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The cleaved extra turn's damage can't be prevented, but it causes no loss")
    void cleavedTurnRemainsUnpreventableButDoesNotCauseLoss() {
        enableAutoStop();
        castCleaved();
        assertThat(gd.damageCantBePreventedThisTurn()).isFalse();

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.damageCantBePreventedThisTurn()).isTrue();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.damageCantBePreventedThisTurn()).isFalse();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("A later cleaved extra turn is taken before the extra turn with the delayed loss")
    void laterCleavedTurnIsTakenBeforeTheTurnWithDelayedLoss() {
        enableAutoStop();
        castNormally();
        castCleaved();

        advanceTurn();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        advanceTurn();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        gd.rulesEvents.occur(new TurnEvent.Cleanup(gd.activePlayerId));
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private AlchemistsGambit castNormally() {
        AlchemistsGambit card = new AlchemistsGambit();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, card, "{1}{R}{R}");
        harness.passBothPriorities();
        return card;
    }

    private void enableAutoStop() {
        Set<TurnStep> stops1 = ConcurrentHashMap.newKeySet();
        stops1.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player1.getId(), stops1);
        Set<TurnStep> stops2 = ConcurrentHashMap.newKeySet();
        stops2.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player2.getId(), stops2);
    }
}
