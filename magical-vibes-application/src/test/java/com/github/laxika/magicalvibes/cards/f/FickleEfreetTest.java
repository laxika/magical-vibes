package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.Entangler;
import com.github.laxika.magicalvibes.cards.s.SporeFrog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FickleEfreet.class, SporeFrog.class, Entangler.class})
class FickleEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking flips at end of combat and transfers control only on a loss")
    void attackingFlipsAtEndOfCombat() {
        Permanent efreet = addCreatureReady(player1, new FickleEfreet());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

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

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        gd.playerBattlefields.get(player1.getId()).remove(efreet);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(coinFlipLogs()).hasSize(1);
    }

    @Test
    @DisplayName("Blocking multiple creatures creates only one delayed coin flip")
    void blockingMultipleCreaturesFlipsOnlyOnce() {
        Permanent firstAttacker = addCreatureReady(player1, new SporeFrog());
        Permanent secondAttacker = addCreatureReady(player1, new SporeFrog());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        Permanent efreet = addCreatureReady(player2, new FickleEfreet());
        Permanent entangler = harness.addToBattlefieldAndReturn(player2, new Entangler());
        entangler.setAttachedTo(efreet.getId());

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
            resolveAllTriggers();
        });
        assertThat(coinFlipLogs()).isEmpty();

        gd.playerBattlefields.get(player2.getId()).remove(efreet);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(coinFlipLogs()).hasSize(1);
    }

    @Test
    @DisplayName("The delayed trigger uses the original controller after control changes")
    void controlChangeDoesNotChangeWhoFlips() {
        Permanent efreet = addCreatureReady(player1, new FickleEfreet());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        gd.playerBattlefields.get(player1.getId()).remove(efreet);
        gd.playerBattlefields.get(player2.getId()).add(efreet);
        gd.stolenCreatures.put(efreet.getId(), player1.getId());
        efreet.setAttacking(false);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(coinFlipLogs()).hasSize(1);
        assertThat(coinFlipLogs().getFirst()).startsWith(gd.playerIdToName.get(player1.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(efreet);
    }

    @Test
    @DisplayName("Leaving before the attack trigger resolves still schedules a coin flip")
    void sourceLeavesBeforeAttackTriggerResolves() {
        Permanent efreet = addCreatureReady(player1, new FickleEfreet());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        gd.playerBattlefields.get(player1.getId()).remove(efreet);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(coinFlipLogs()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(efreet);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(efreet);
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
