package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuestForRenewal.class, GrizzlyBears.class, Forest.class})
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

        harness.performUntapStep(player2);

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

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The quest counter may be declined")
    void questCounterMayBeDeclined() {
        Permanent quest = addQuest();
        Permanent creature = addReadyCreature(player1);

        tapAndCollectTriggers(creature);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Tapping an opposing creature does not trigger the quest")
    void opposingCreatureDoesNotTrigger() {
        Permanent quest = addQuest();
        Permanent creature = addReadyCreature(player2);

        tapAndCollectTriggers(creature);

        assertThat(gd.stack).isEmpty();
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Quest counters continue accumulating after four")
    void countersAreNotCappedAtFour() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 4);
        Permanent creature = addReadyCreature(player1);

        tapAndCollectTriggers(creature);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(5);
    }

    @Test
    @DisplayName("Five quest counters untap every controlled creature without using the stack")
    void aboveThresholdUntapsAllCreatures() {
        addQuest().setCounterCount(CounterType.QUEST, 5);
        Permanent first = addReadyCreature(player1);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.tap();
        second.tap();

        harness.performUntapStep(player2);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(second.isSummoningSick()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Falling below four counters disables the extra untap")
    void losingThresholdDisablesUntapping() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 4);
        Permanent creature = addReadyCreature(player1);
        creature.tap();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();

        creature.tap();
        quest.setCounterCount(CounterType.QUEST, 3);
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The quest does not untap opposing creatures during its controller's untap step")
    void doesNotUntapOpposingCreatures() {
        addQuest().setCounterCount(CounterType.QUEST, 4);
        Permanent opposingCreature = addReadyCreature(player2);
        opposingCreature.tap();

        harness.performUntapStep(player1);

        assertThat(opposingCreature.isTapped()).isTrue();
    }

    private Permanent addQuest() {
        return harness.addToBattlefieldAndReturn(player1, new QuestForRenewal());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void tapAndCollectTriggers(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
