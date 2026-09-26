package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AetherSnap;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DivineIntervention.class, AetherSnap.class})
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

    private Permanent addIntervention() {
        return harness.enterBattlefieldAndReturn(player1, new DivineIntervention());
    }
}
