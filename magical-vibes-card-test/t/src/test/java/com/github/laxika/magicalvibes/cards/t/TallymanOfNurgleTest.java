package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VilisBrokerOfBlood;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TallymanOfNurgle.class, Forest.class})
class TallymanOfNurgleTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger at the end step when no creature died")
    void doesNotTriggerWithoutCreatureDeath() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new TallymanOfNurgle());

        runToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Draws a card and loses 1 life after a creature dies")
    void drawsOneAndLosesOneLife() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new TallymanOfNurgle());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        runToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Draws seven cards and loses 7 life after seven creatures die")
    void drawsSevenAndLosesSevenLife() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new TallymanOfNurgle());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 7);

        runToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
    }

    @Test
    @CardUsed({TallymanOfNurgle.class, Forest.class, VilisBrokerOfBlood.class})
    @DisplayName("Seven deaths cause a single seven-life loss event")
    void sevenLifeLossTriggersVilisOnlyOnce() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, java.util.stream.IntStream.range(0, 14)
                .mapToObj(i -> new Forest()).toList());
        harness.addToBattlefield(player1, new TallymanOfNurgle());
        harness.addToBattlefield(player1, new VilisBrokerOfBlood());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passBothPriorities());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        harness.assertLife(player1, 13);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(14);
    }

    @Test
    @DisplayName("Six deaths across both players still draw only one card")
    void sixDeathsDoNotReachThreshold() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player1, new TallymanOfNurgle());
        gd.creatureDeathCountThisTurn.put(player1.getId(), 3);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 3);

        runToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void opponentEndStepDoesNotTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new TallymanOfNurgle());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 7);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Crossing the seven-death threshold before resolution draws seven cards")
    void thresholdIsCheckedAtResolution() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> new Forest()).toList());
        harness.addToBattlefield(player1, new TallymanOfNurgle());
        gd.creatureDeathCountThisTurn.put(player1.getId(), 3);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        gd.creatureDeathCountThisTurn.put(player2.getId(), 5);
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passBothPriorities());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        harness.assertLife(player1, 13);
    }

    private void runToEndStep() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
