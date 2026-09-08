package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FickleEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking flips at end of combat and transfers control only on a loss")
    void attackingFlipsAtEndOfCombat() {
        Permanent efreet = addReady(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(coinFlipLogs()).isEmpty();

        declareNoBlockers();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(coinFlipLogs()).hasSize(1);
        assertControlMatchesFlip(efreet, player1);
    }

    @Test
    @DisplayName("Blocking creates the same end-of-combat flip")
    void blockingFlipsAtEndOfCombat() {
        Permanent attacker = addReady(player1, harmlessAttacker());
        attacker.setAttacking(true);
        Permanent efreet = addReady(player2);

        declareNoBlockers(new BlockerAssignment(0, 0));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(coinFlipLogs()).hasSize(1);
        assertControlMatchesFlip(efreet, player2);
    }

    @Test
    @DisplayName("The delayed flip still happens if the Efreet leaves before end of combat")
    void delayedFlipSurvivesSourceLeaving() {
        Permanent efreet = addReady(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(efreet);

        declareNoBlockers();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(coinFlipLogs()).hasSize(1);
    }

    private void declareNoBlockers(BlockerAssignment... assignments) {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(assignments));
    }

    private Permanent addReady(Player player) {
        return addReady(player, new FickleEfreet());
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private static Card harmlessAttacker() {
        Card card = new Card();
        card.setName("Harmless Attacker");
        card.setType(CardType.CREATURE);
        card.setPower(0);
        card.setToughness(1);
        return card;
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
