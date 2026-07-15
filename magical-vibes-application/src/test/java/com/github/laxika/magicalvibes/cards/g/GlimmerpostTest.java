package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlimmerpostTest extends BaseCardTest {

    // ===== ETB trigger =====

    @Test
    @DisplayName("Playing Glimmerpost puts ETB trigger on the stack")
    void playingPutsEtbTriggerOnStack() {
        playGlimmerpost(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Glimmerpost");
    }

    @Test
    @DisplayName("ETB gains 1 life when Glimmerpost is the only Locus")
    void etbGainsOneLifeWithSingleLocus() {
        harness.setLife(player1, 20);
        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("ETB gains 2 life when a second Locus is already on the battlefield")
    void etbGainsTwoLifeWithTwoLoci() {
        harness.addToBattlefield(player1, new Glimmerpost());
        harness.setLife(player1, 20);

        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("ETB counts Loci controlled by all players")
    void etbCountsAllPlayersLoci() {
        harness.addToBattlefield(player2, new Glimmerpost());
        harness.setLife(player1, 20);

        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        // Counts opponent's Locus + own Glimmerpost = 2
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("ETB gains 3 life with three Loci on the battlefield")
    void etbGainsThreeLifeWithThreeLoci() {
        harness.addToBattlefield(player1, new Glimmerpost());
        harness.addToBattlefield(player2, new Glimmerpost());
        harness.setLife(player1, 20);

        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Game log records life gain from ETB")
    void gameLogRecordsLifeGain() {
        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("gains 1 life"));
    }

    // ===== Land enters battlefield =====

    @Test
    @DisplayName("Glimmerpost enters the battlefield as a permanent")
    void entersBattlefieldAsPermanent() {
        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Glimmerpost");
    }

    @Test
    @DisplayName("Stack is empty after ETB fully resolves")
    void stackEmptyAfterResolution() {
        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void playGlimmerpost(Player player) {
        harness.setHand(player, List.of(new Glimmerpost()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player, 0);
    }
}
