package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiggingRunner.class, JungleDelver.class})
class RiggingRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Cast without raid — enters as 1/1 with no counters")
    void castWithoutRaid() {
        harness.setHand(player1, List.of(new RiggingRunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent runner = findPermanent(player1, "Rigging Runner");
        assertThat(runner).isNotNull();
        assertThat(runner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cast without raid — creature enters battlefield")
    void creatureEntersWithoutRaid() {
        harness.setHand(player1, List.of(new RiggingRunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rigging Runner");
    }

    @Test
    @DisplayName("Cast with raid — enters as 2/2 with one +1/+1 counter")
    void castWithRaid() {
        markAttackedThisTurn();
        harness.setHand(player1, List.of(new RiggingRunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent runner = findPermanent(player1, "Rigging Runner");
        assertThat(runner).isNotNull();
        assertThat(runner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cast with raid — log message includes counter info")
    void castWithRaidLogMessage() {
        markAttackedThisTurn();
        harness.setHand(player1, List.of(new RiggingRunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Rigging Runner") && log.contains("+1/+1 counter"));
    }

    @Test
    @DisplayName("Raid counter is a replacement effect — no ETB trigger on stack")
    void raidCounterIsReplacementEffect() {
        markAttackedThisTurn();
        harness.setHand(player1, List.of(new RiggingRunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Stack should be empty after resolution — no triggered ability
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent attacking does not count for your raid")
    void opponentAttackDoesNotCountForRaid() {
        // Only opponent attacked — player1's raid should not be met
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        harness.setHand(player1, List.of(new RiggingRunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent runner = findPermanent(player1, "Rigging Runner");
        assertThat(runner).isNotNull();
        assertThat(runner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal combat damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        harness.setHand(player1, List.of(new RiggingRunner()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        findPermanent(player1, "Rigging Runner").setSummoningSick(false);
        harness.addToBattlefield(player2, new JungleDelver());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Jungle Delver");
        harness.assertOnBattlefield(player1, "Rigging Runner");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Raid still counts when the attacking creature dies in combat")
    void raidCountsAfterAttackerDies() {
        addCreatureReady(player1, new JungleDelver());
        harness.addToBattlefield(player2, new RiggingRunner());
        harness.setHand(player1, List.of(new RiggingRunner()));
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.assertInGraveyard(player1, "Jungle Delver");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Rigging Runner")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }
}
