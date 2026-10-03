package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EDELonesomeEyebot.class, SolemnSimulacrum.class, SwordsToPlowshares.class})
class EDELonesomeEyebotTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with more creatures than quest counters adds a quest counter")
    void attackAddsQuestCounterWhenAttackerCountIsGreater() {
        Permanent edE = addReadyEdE(player1);
        addCreatureReady(player1, new SolemnSimulacrum());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(edE.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack trigger does not add a quest counter when the counts are equal")
    void attackDoesNotAddQuestCounterWhenCountsAreEqual() {
        Permanent edE = addReadyEdE(player1);
        edE.setCounterCount(CounterType.QUEST, 1);
        addCreatureReady(player1, new SolemnSimulacrum());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(edE.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing ED-E draws one card plus one for each quest counter")
    void sacrificeDrawsBaseCardAndQuestCounterCards() {
        Permanent edE = addReadyEdE(player1);
        edE.setCounterCount(CounterType.QUEST, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        harness.assertNotOnBattlefield(player1, "ED-E, Lonesome Eyebot");
        harness.assertInGraveyard(player1, "ED-E, Lonesome Eyebot");
    }

    @Test
    void multipleAttackersAddOnlyOneQuestCounter() {
        Permanent edE = addReadyEdE(player1);
        edE.setCounterCount(CounterType.QUEST, 1);
        addCreatureReady(player1, new SolemnSimulacrum());
        addCreatureReady(player1, new SolemnSimulacrum());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(edE.getCounterCount(CounterType.QUEST)).isEqualTo(2);
    }

    @Test
    void opponentsAttackDoesNotAddQuestCounter() {
        Permanent edE = addReadyEdE(player1);
        addCreatureReady(player2, new SolemnSimulacrum());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(edE.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void questCounterConditionIsCheckedAgainAtResolution() {
        Permanent edE = addReadyEdE(player1);
        addCreatureReady(player1, new SolemnSimulacrum());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        edE.setCounterCount(CounterType.QUEST, 1);
        resolveAllTriggers();

        assertThat(edE.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    void removingAnAttackerCanMakeTheConditionFalseAtResolution() {
        Permanent edE = addReadyEdE(player1);
        edE.setCounterCount(CounterType.QUEST, 1);
        Permanent first = addCreatureReady(player1, new SolemnSimulacrum());
        addCreatureReady(player1, new SolemnSimulacrum());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(List.of(1, 2));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, first.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        assertThat(edE.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    void noAttackersDoesNotTrigger() {
        Permanent edE = addReadyEdE(player1);

        declareAttackers(List.of());
        resolveAllTriggers();

        assertThat(edE.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void tappedSummoningSickEdEWithNoQuestCountersDrawsOnlyOneCard() {
        Permanent edE = addReadyEdE(player1);
        edE.setTapped(true);
        edE.setSummoningSick(true);
        edE.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertNotOnBattlefield(player1, "ED-E, Lonesome Eyebot");
        harness.assertInGraveyard(player1, "ED-E, Lonesome Eyebot");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private Permanent addReadyEdE(Player player) {
        return addCreatureReady(player, new EDELonesomeEyebot());
    }
}
