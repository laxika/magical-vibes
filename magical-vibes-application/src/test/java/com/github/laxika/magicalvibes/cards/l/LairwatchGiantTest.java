package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LairwatchGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking two creatures triggers once and grants first strike")
    void blockingTwoCreaturesGrantsFirstStrike() {
        Permanent giant = addReadyGiant(player2);
        Permanent attacker1 = addReadyBears(player1);
        Permanent attacker2 = addReadyBears(player1);
        attacker1.setAttacking(true);
        attacker2.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        ));

        long triggerCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .filter(e -> e.getCard().getName().equals("Lairwatch Giant"))
                .count();
        assertThat(triggerCount).isEqualTo(1);

        StackEntry trigger = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Lairwatch Giant"))
                .findFirst()
                .orElseThrow();
        assertThat(trigger.getSourcePermanentId()).isEqualTo(giant.getId());

        assertThat(giant.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();
        assertThat(giant.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Blocking only one creature does not trigger and grants no first strike")
    void blockingOneCreatureDoesNotTrigger() {
        Permanent giant = addReadyGiant(player2);
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack)
                .noneMatch(e -> e.getCard().getName().equals("Lairwatch Giant"));
        assertThat(giant.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent giant = addReadyGiant(player2);
        Permanent attacker1 = addReadyBears(player1);
        Permanent attacker2 = addReadyBears(player1);
        attacker1.setAttacking(true);
        attacker2.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        ));
        harness.passBothPriorities();
        assertThat(giant.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(giant.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addReadyGiant(Player player) {
        Permanent perm = new Permanent(new LairwatchGiant());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
