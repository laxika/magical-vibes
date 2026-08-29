package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuestForRenewalTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a creature you control offers a quest counter")
    void tappingControlledCreatureOffersQuestCounter() {
        Permanent quest = addQuest();
        Permanent creature = addReadyCreature(player1);

        tapAndCollectTriggers(creature);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping a noncreature you control does not offer a quest counter")
    void tappingNoncreatureDoesNotTrigger() {
        Permanent quest = addQuest();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        tapAndCollectTriggers(land);

        assertThat(gd.stack).isEmpty();
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Fewer than four quest counters do not untap creatures during an opponent's untap step")
    void fewerThanFourQuestCountersDoNotUntapCreatures() {
        addQuest().setCounterCount(CounterType.QUEST, 3);
        Permanent creature = addReadyCreature(player1);
        creature.tap();

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Four quest counters untap creatures but not lands during an opponent's untap step")
    void fourQuestCountersUntapControlledCreatures() {
        addQuest().setCounterCount(CounterType.QUEST, 4);
        Permanent creature = addReadyCreature(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        creature.tap();
        land.tap();

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
    }

    private Permanent addQuest() {
        return harness.addToBattlefieldAndReturn(player1, new QuestForRenewal());
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private void tapAndCollectTriggers(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
