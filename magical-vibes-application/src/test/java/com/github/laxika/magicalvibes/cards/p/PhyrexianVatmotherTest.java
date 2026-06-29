package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GiveControllerPoisonCountersEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianVatmotherTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Phyrexian Vatmother has upkeep poison trigger")
    void hasUpkeepPoisonTrigger() {
        PhyrexianVatmother card = new PhyrexianVatmother();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(GiveControllerPoisonCountersEffect.class);
    }

    // ===== Upkeep poison trigger =====

    @Test
    @DisplayName("Controller gets 1 poison counter at upkeep")
    void controllerGetsPoisonCounterAtUpkeep() {
        harness.addToBattlefield(player1, new PhyrexianVatmother());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent does not get poison from controller's Vatmother upkeep trigger")
    void opponentDoesNotGetPoisonFromControllerUpkeep() {
        harness.addToBattlefield(player1, new PhyrexianVatmother());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new PhyrexianVatmother());

        advanceToUpkeep(player2); // opponent's upkeep
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Poison counters accumulate over multiple upkeeps")
    void poisonCountersAccumulate() {
        harness.addToBattlefield(player1, new PhyrexianVatmother());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller loses the game at 10 poison counters from Vatmother")
    void controllerLosesAtTenPoison() {
        harness.addToBattlefield(player1, new PhyrexianVatmother());
        gd.playerPoisonCounters.put(player1.getId(), 9);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Poison counter from upkeep trigger is logged")
    void poisonCounterIsLogged() {
        harness.addToBattlefield(player1, new PhyrexianVatmother());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.gameLog).anyMatch(log -> log.contains("gets 1 poison counter"));
    }
}
