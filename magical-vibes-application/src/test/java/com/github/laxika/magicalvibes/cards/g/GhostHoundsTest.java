package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AysenBureaucrats;
import com.github.laxika.magicalvibes.cards.d.DwarvenPony;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhostHounds.class, AysenBureaucrats.class, DwarvenPony.class})
class GhostHoundsTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a white creature gives Ghost Hounds first strike")
    void blocksWhiteCreatureGrantsFirstStrike() {
        Permanent attacker = addCreatureReady(player1, new AysenBureaucrats());
        attacker.setAttacking(true);
        Permanent hounds = addCreatureReady(player2, new GhostHounds());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hounds, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Blocking a non-white creature gives Ghost Hounds nothing")
    void blocksNonWhiteCreatureDoesNothing() {
        Permanent attacker = addCreatureReady(player1, new DwarvenPony());
        attacker.setAttacking(true);
        Permanent hounds = addCreatureReady(player2, new GhostHounds());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gqs.hasKeyword(gd, hounds, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Becoming blocked by a white creature gives Ghost Hounds first strike")
    void becomesBlockedByWhiteCreatureGrantsFirstStrike() {
        Permanent hounds = addCreatureReady(player1, new GhostHounds());
        hounds.setAttacking(true);
        addCreatureReady(player2, new AysenBureaucrats());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hounds, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Becoming blocked by a non-white creature gives Ghost Hounds nothing")
    void becomesBlockedByNonWhiteCreatureDoesNothing() {
        Permanent hounds = addCreatureReady(player1, new GhostHounds());
        hounds.setAttacking(true);
        addCreatureReady(player2, new DwarvenPony());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gqs.hasKeyword(gd, hounds, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance keeps Ghost Hounds untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent hounds = addCreatureReady(player1, new GhostHounds());

        declareAttackers(List.of(0));

        assertThat(hounds.isTapped()).isFalse();
    }

    @Test
    @DisplayName("First strike granted by a white combat opponent wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new AysenBureaucrats());
        attacker.setAttacking(true);
        Permanent hounds = addCreatureReady(player2, new GhostHounds());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hounds, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hounds, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Becoming blocked by two white creatures creates one trigger for each blocker")
    void becomesBlockedByTwoWhiteCreaturesTriggersForEachBlocker() {
        Permanent hounds = addCreatureReady(player1, new GhostHounds());
        hounds.setAttacking(true);
        addCreatureReady(player2, new AysenBureaucrats());
        addCreatureReady(player2, new AysenBureaucrats());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .filter(entry -> entry.getCard().getName().equals("Ghost Hounds"))
                .count()).isEqualTo(2);
    }
}
