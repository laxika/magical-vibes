package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GoblinTraprunner.class)
class GoblinTraprunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates one tapped and attacking Goblin per won coin flip")
    void attackCreatesGoblinTokensForWonFlips() {
        addCreatureReady(player1, new GoblinTraprunner());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveTokenAttackChoices();

        int heads = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("flips 3 coins for Goblin Traprunner"))
                .mapToInt(this::headsFromLog)
                .findFirst()
                .orElseThrow();
        List<Permanent> tokens = findPermanents(player1, "Goblin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(heads);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
        });
    }

    @Test
    @DisplayName("Goblin Traprunner does not trigger when it does not attack")
    void noTriggerWithoutAttacking() {
        addCreatureReady(player1, new GoblinTraprunner());

        declareAttackers(List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    private void resolveTokenAttackChoices() {
        while (gd.interaction.isAwaitingInput()) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player2.getId());
        }
    }

    private int headsFromLog(String log) {
        return Integer.parseInt(log.substring(log.indexOf(": ") + 2, log.indexOf(" heads")));
    }
}
