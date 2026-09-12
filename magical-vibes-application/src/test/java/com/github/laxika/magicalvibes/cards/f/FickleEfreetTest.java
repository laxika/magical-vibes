package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SporeFrog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FickleEfreet.class, SporeFrog.class})
class FickleEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking flips at end of combat and transfers control only on a loss")
    void attackingFlipsAtEndOfCombat() {
        Permanent efreet = addCreatureReady(player1, new FickleEfreet());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(coinFlipLogs()).isEmpty();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(coinFlipLogs()).hasSize(1);
        assertControlMatchesFlip(efreet, player1);
    }

    @Test
    @DisplayName("Blocking creates the same end-of-combat flip")
    void blockingFlipsAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new SporeFrog());
        attacker.setAttacking(true);
        Permanent efreet = addCreatureReady(player2, new FickleEfreet());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        resolveCombat();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(coinFlipLogs()).hasSize(1);
        assertControlMatchesFlip(efreet, player2);
    }

    @Test
    @DisplayName("A creature that neither attacks nor blocks does not flip a coin")
    void uninvolvedCreatureDoesNotFlip() {
        Permanent efreet = addCreatureReady(player1, new FickleEfreet());

        declareAttackers(List.of());
        resolveAllTriggers();

        assertThat(coinFlipLogs()).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(efreet);
    }

    @Test
    @DisplayName("The delayed flip still happens if the Efreet leaves before end of combat")
    void delayedFlipSurvivesSourceLeaving() {
        Permanent efreet = addCreatureReady(player1, new FickleEfreet());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(efreet);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(coinFlipLogs()).hasSize(1);
    }

    private List<String> coinFlipLogs() {
        return gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Fickle Efreet"))
                .toList();
    }

    private void assertControlMatchesFlip(Permanent efreet, Player flipper) {
        boolean won = coinFlipLogs().getFirst().contains("wins the coin flip");
        Player expectedController = won ? flipper : (flipper == player1 ? player2 : player1);
        assertThat(gd.playerBattlefields.get(expectedController.getId())).contains(efreet);
    }
}
