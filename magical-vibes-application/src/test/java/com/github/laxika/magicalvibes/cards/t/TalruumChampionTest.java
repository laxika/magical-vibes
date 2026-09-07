package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TalruumChampion.class, YouthfulKnight.class})
class TalruumChampionTest extends BaseCardTest {

    @Test
    @DisplayName("When the Champion blocks a creature, that attacker loses first strike until end of turn")
    void blocksCreatureRemovesFirstStrike() {
        Permanent attacker = addCreatureReady(player1, new YouthfulKnight());
        attacker.setAttacking(true);
        addCreatureReady(player2, new TalruumChampion());

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("When the Champion becomes blocked by a creature, that blocker loses first strike until end of turn")
    void becomesBlockedRemovesFirstStrike() {
        Permanent champion = addCreatureReady(player1, new TalruumChampion());
        champion.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new YouthfulKnight());

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isTrue();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("When the Champion becomes blocked by multiple creatures, each blocker loses first strike")
    void becomesBlockedRemovesFirstStrikeFromEachBlocker() {
        Permanent champion = addCreatureReady(player1, new TalruumChampion());
        champion.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new YouthfulKnight());
        Permanent secondBlocker = addCreatureReady(player2, new YouthfulKnight());
        Permanent bystander = addCreatureReady(player2, new YouthfulKnight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, firstBlocker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondBlocker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bystander, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First-strike loss clears during end-of-turn cleanup")
    void lossClearsAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new YouthfulKnight());
        attacker.setAttacking(true);
        addCreatureReady(player2, new TalruumChampion());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
    }
}
