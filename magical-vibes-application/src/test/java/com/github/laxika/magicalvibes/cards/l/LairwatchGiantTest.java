package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LairwatchGiant.class, AxegrinderGiant.class})
class LairwatchGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking two creatures triggers once and grants first strike")
    void blockingTwoCreaturesGrantsFirstStrike() {
        Permanent giant = addCreatureReady(player2, new LairwatchGiant());
        addCreatureReady(player1, new AxegrinderGiant());
        addCreatureReady(player1, new AxegrinderGiant());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
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
        resolveAllTriggers();
        assertThat(giant.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Blocking only one creature does not trigger and grants no first strike")
    void blockingOneCreatureDoesNotTrigger() {
        Permanent giant = addCreatureReady(player2, new LairwatchGiant());
        addCreatureReady(player1, new AxegrinderGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack)
                .noneMatch(e -> e.getCard().getName().equals("Lairwatch Giant"));
        assertThat(giant.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent giant = addCreatureReady(player2, new LairwatchGiant());
        addCreatureReady(player1, new AxegrinderGiant());
        addCreatureReady(player1, new AxegrinderGiant());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        ));
        resolveAllTriggers();
        assertThat(giant.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(giant.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The additional block permits two attackers but not three")
    void cannotBlockThreeCreatures() {
        addCreatureReady(player2, new LairwatchGiant());
        addCreatureReady(player1, new AxegrinderGiant());
        addCreatureReady(player1, new AxegrinderGiant());
        addCreatureReady(player1, new AxegrinderGiant());

        declareAttackersAndPrepareBlockers(List.of(0, 1, 2));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("assigned too many times");
    }

    @Test
    @DisplayName("A multi-block trigger grants first strike only to its source")
    void firstStrikeIsNotGrantedToOtherGiants() {
        Permanent multiBlocker = addCreatureReady(player2, new LairwatchGiant());
        Permanent singleBlocker = addCreatureReady(player2, new LairwatchGiant());
        addCreatureReady(player1, new AxegrinderGiant());
        addCreatureReady(player1, new AxegrinderGiant());
        addCreatureReady(player1, new AxegrinderGiant());

        declareAttackersAndPrepareBlockers(List.of(0, 1, 2));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 2)
        ));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(multiBlocker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(singleBlocker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The additional block does not apply to other creatures")
    void otherCreaturesCannotBlockTwoAttackers() {
        addCreatureReady(player2, new LairwatchGiant());
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new AxegrinderGiant());
        addCreatureReady(player1, new AxegrinderGiant());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1)
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("assigned too many times");
    }
}
