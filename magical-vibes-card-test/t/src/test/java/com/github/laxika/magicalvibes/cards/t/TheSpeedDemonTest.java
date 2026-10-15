package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSpeedDemon.class})
class TheSpeedDemonTest extends BaseCardTest {

    @Test
    void drawsAndLosesLifeEqualToControllerSpeedAtEndStep() {
        addCreatureReady(player1, new TheSpeedDemon());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheSpeedDemon(), new TheSpeedDemon(), new TheSpeedDemon()));
        harness.setLife(player1, 20);
        gd.playerSpeeds.put(player1.getId(), 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4})
    void drawsAndLosesLifeAtStartingAndMaximumSpeed(int speed) {
        addCreatureReady(player1, new TheSpeedDemon());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheSpeedDemon(), new TheSpeedDemon(),
                new TheSpeedDemon(), new TheSpeedDemon(), new TheSpeedDemon()));
        harness.setLife(player1, 20);
        gd.playerSpeeds.put(player1.getId(), speed);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(speed);
        harness.assertLife(player1, 20 - speed);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        addCreatureReady(player1, new TheSpeedDemon());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheSpeedDemon(), new TheSpeedDemon()));
        harness.setLife(player1, 20);
        gd.playerSpeeds.put(player1.getId(), 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void usesSpeedAtResolutionEvenWhenSourceHasLeftBattlefield() {
        var demon = addCreatureReady(player1, new TheSpeedDemon());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheSpeedDemon(), new TheSpeedDemon(),
                new TheSpeedDemon(), new TheSpeedDemon()));
        harness.setLife(player1, 20);
        gd.playerSpeeds.put(player1.getId(), 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(demon);
        gd.playerGraveyards.get(player1.getId()).add(demon.getCard());
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 17);
    }

    @Test
    void startsEnginesAndDrawsAtSpeedOneWithoutPriorSpeed() {
        addCreatureReady(player1, new TheSpeedDemon());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheSpeedDemon(), new TheSpeedDemon()));
        harness.setLife(player1, 20);
        gd.playerSpeeds.remove(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
    }
}
