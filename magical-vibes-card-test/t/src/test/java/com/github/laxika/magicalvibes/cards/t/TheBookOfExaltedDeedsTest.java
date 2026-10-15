package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DawnbringerCleric;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheBookOfExaltedDeeds.class, DawnbringerCleric.class})
class TheBookOfExaltedDeedsTest extends BaseCardTest {

    @Test
    void createsAngelAtEndStepAfterGainingThreeLife() {
        harness.addToBattlefield(player1, new TheBookOfExaltedDeeds());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Angel")).hasSize(1);
        Permanent angel = findPermanent(player1, "Angel");
        assertThat(angel.getCounterCount(CounterType.ENLIGHTENED)).isZero();
    }

    @Test
    void abilityExilesBookAndGrantsGameLockAbilityToTargetAngel() {
        addBookAndCreateAngel();
        Permanent angel = findPermanent(player1, "Angel");
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, angel.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "The Book of Exalted Deeds");
        assertThat(angel.getCounterCount(CounterType.ENLIGHTENED)).isEqualTo(1);

        harness.setLife(player1, 0);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.setLife(player1, 20);
        harness.setLife(player2, 0);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void abilityRequiresAnAngelTarget() {
        addBookReady(player1);
        Permanent bears = addCreatureReady(player1, new DawnbringerCleric());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityIsSorcerySpeedOnly() {
        Permanent angel = addBookAndCreateAngel();
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void doesNotTriggerBelowThreeLifeGained() {
        addBookReady(player1);
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }

    @Test
    void doesNotTriggerOnOpponentsEndStep() {
        addBookReady(player1);
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }

    @Test
    void createsOnlyOneAngelAfterGainingMoreThanThreeLifeAndLosingLife() {
        addBookReady(player1);
        gd.lifeGainedThisTurn.put(player1.getId(), 9);
        harness.setLife(player1, 10);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Angel")).hasSize(1);
    }

    @Test
    void counterRemovalDoesNotRemoveGrantedAbility() {
        Permanent angel = addBookAndCreateAngel();
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, angel.getId());
        harness.passBothPriorities();

        angel.setCounterCount(CounterType.ENLIGHTENED, 0);
        harness.setLife(player1, 0);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void exilesBookAsCostBeforeAbilityResolves() {
        Permanent angel = addBookAndCreateAngel();
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, angel.getId());

        harness.assertNotOnBattlefield(player1, "The Book of Exalted Deeds");
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card() instanceof TheBookOfExaltedDeeds
                        && entry.ownerId().equals(player1.getId()));
        assertThat(angel.getCounterCount(CounterType.ENLIGHTENED)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(angel.getCounterCount(CounterType.ENLIGHTENED)).isEqualTo(1);
    }

    @Test
    void cannotActivateTappedBook() {
        Permanent angel = addBookAndCreateAngel();
        findPermanent(player1, "The Book of Exalted Deeds").tap();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(angel.getCounterCount(CounterType.ENLIGHTENED)).isZero();
    }

    @Test
    void cannotActivateWithoutThreeWhiteMana() {
        Permanent angel = addBookAndCreateAngel();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "The Book of Exalted Deeds");
    }

    @Test
    void canEnlightenOpponentsAngelAndProtectsItsController() {
        addBookReady(player2);
        gd.lifeGainedThisTurn.put(player2.getId(), 3);
        advanceToEndStep(player2);
        harness.passBothPriorities();
        Permanent angel = findPermanent(player2, "Angel");
        addBookReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, angel.getId());
        harness.passBothPriorities();

        assertThat(angel.getCounterCount(CounterType.ENLIGHTENED)).isEqualTo(1);
        harness.setLife(player2, 0);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.setLife(player2, 20);
        harness.setLife(player1, 0);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void protectionEndsWhenEnlightenedAngelLeavesBattlefield() {
        Permanent angel = addBookAndCreateAngel();
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, angel.getId());
        harness.passBothPriorities();
        harness.setLife(player1, 0);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, angel));
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void removingTargetBeforeResolutionDoesNotProtectPlayerOrRefundBook() {
        Permanent angel = addBookAndCreateAngel();
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, angel.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, angel));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "The Book of Exalted Deeds");
        assertThat(gd.stack).isEmpty();
        harness.setLife(player1, 0);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void createsAngelEvenIfBookLeavesAfterTriggering() {
        Permanent book = addBookReady(player1);
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, book));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Angel")).hasSize(1);
        harness.assertNotOnBattlefield(player1, "The Book of Exalted Deeds");
    }

    @Test
    void enlightenedAngelPreventsLosingToPoison() {
        Permanent angel = addBookAndCreateAngel();
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, angel.getId());
        harness.passBothPriorities();

        gd.playerPoisonCounters.put(player1.getId(), 10);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, angel));
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void cannotActivateDuringOwnEndStep() {
        Permanent angel = addBookAndCreateAngel();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertOnBattlefield(player1, "The Book of Exalted Deeds");
        assertThat(angel.getCounterCount(CounterType.ENLIGHTENED)).isZero();
    }

    private Permanent addBookAndCreateAngel() {
        addBookReady(player1);
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return findPermanent(player1, "Angel");
    }

    private Permanent addBookReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new TheBookOfExaltedDeeds());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
