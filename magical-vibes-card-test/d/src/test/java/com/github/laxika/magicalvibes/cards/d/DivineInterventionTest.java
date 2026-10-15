package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AetherSnap;
import com.github.laxika.magicalvibes.cards.c.Clockspinning;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DivineIntervention.class, AetherSnap.class, Clockspinning.class})
class DivineInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two intervention counters")
    void entersWithTwoInterventionCounters() {
        Permanent intervention = addIntervention();

        assertThat(intervention.getCounterCount(CounterType.INTERVENTION)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes one intervention counter during its controller's upkeep")
    void removesOneCounterDuringUpkeep() {
        Permanent intervention = addIntervention();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(intervention.getCounterCount(CounterType.INTERVENTION)).isEqualTo(1);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Declares a draw when the last intervention counter is removed")
    void declaresDrawWhenLastCounterIsRemoved() {
        Permanent intervention = addIntervention();
        intervention.setCounterCount(CounterType.INTERVENTION, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(intervention.getCounterCount(CounterType.INTERVENTION)).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.DRAW);
    }

    @Test
    @DisplayName("Does not remove a counter during an opponent's upkeep")
    void doesNotRemoveCounterDuringOpponentsUpkeep() {
        Permanent intervention = addIntervention();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(intervention.getCounterCount(CounterType.INTERVENTION)).isEqualTo(2);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Declares a draw when its controller removes the last counter with another effect")
    void declaresDrawWhenControllerRemovesLastCounterWithAnotherEffect() {
        Permanent intervention = addIntervention();
        intervention.setCounterCount(CounterType.INTERVENTION, 1);

        harness.setHand(player1, List.of(new AetherSnap()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(intervention.getCounterCount(CounterType.INTERVENTION)).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.DRAW);
    }

    @Test
    @DisplayName("Does not declare a draw when it has no intervention counters")
    void doesNotDrawWithoutCounters() {
        Permanent intervention = addIntervention();
        intervention.setCounterCount(CounterType.INTERVENTION, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The draw trigger resolves separately from the upkeep counter removal")
    void drawWaitsForItsOwnTriggerToResolve() {
        Permanent intervention = addIntervention();
        intervention.setCounterCount(CounterType.INTERVENTION, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(intervention.getCounterCount(CounterType.INTERVENTION)).isZero();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.DRAW);
    }

    @Test
    @DisplayName("Removing both intervention counters at once still triggers a draw")
    void removingBothCountersAtOnceTriggersDraw() {
        Permanent intervention = addIntervention();
        harness.setHand(player1, List.of(new AetherSnap()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(intervention.getCounterCount(CounterType.INTERVENTION)).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.DRAW);
    }

    @Test
    @DisplayName("Clockspinning controlled by the enchantment's controller triggers a draw")
    void controllerClockspinningRemovingLastCounterTriggersDraw() {
        Permanent intervention = addIntervention();
        intervention.setCounterCount(CounterType.INTERVENTION, 1);

        removeCounterWithClockspinning(player1, intervention);
        resolveAllTriggers();

        assertThat(intervention.getCounterCount(CounterType.INTERVENTION)).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.DRAW);
    }

    @Test
    @DisplayName("An opponent removing the last counter with Clockspinning does not trigger a draw")
    void opponentClockspinningRemovingLastCounterDoesNotDraw() {
        Permanent intervention = addIntervention();
        intervention.setCounterCount(CounterType.INTERVENTION, 1);

        removeCounterWithClockspinning(player2, intervention);
        resolveAllTriggers();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(intervention.getCounterCount(CounterType.INTERVENTION)).isZero();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Removing the first counter with Clockspinning does not trigger a draw")
    void controllerClockspinningRemovingFirstCounterDoesNotDraw() {
        Permanent intervention = addIntervention();

        removeCounterWithClockspinning(player1, intervention);
        resolveAllTriggers();

        assertThat(intervention.getCounterCount(CounterType.INTERVENTION)).isEqualTo(1);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    private void removeCounterWithClockspinning(Player player, Permanent intervention) {
        harness.setHand(player, List.of(new Clockspinning()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player, 0, intervention.getId());
        harness.handleListChoice(player, "intervention counters");
        harness.handleListChoice(player, "REMOVE");
    }

    private Permanent addIntervention() {
        return harness.enterBattlefieldAndReturn(player1, new DivineIntervention());
    }
}
