package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Timebender.class, AncestralVision.class})
class TimebenderTest extends BaseCardTest {

    @Test
    void turningFaceUpRemovesTwoTimeCountersFromTargetPermanent() {
        turnFaceUp();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Timebender());
        target.setCounterCount(CounterType.TIME, 3);

        chooseModeAndTarget("Remove two time counters", target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void turningFaceUpAddsTwoTimeCountersToTargetPermanentWithTimeCounter() {
        Permanent timebender = prepareFaceDownTimebender();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Timebender());
        target.setCounterCount(CounterType.TIME, 1);
        turnFaceUp(timebender);

        chooseModeAndTarget("Put two time counters", target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    void turningFaceUpCanRemoveTwoTimeCountersFromSuspendedCard() {
        turnFaceUp();
        AncestralVision target = suspendedAncestralVision(3);

        chooseModeAndTarget("Remove two time counters", target.getId());

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 1);
    }

    @Test
    void turningFaceUpAddsTwoTimeCountersToSuspendedCard() {
        Permanent timebender = prepareFaceDownTimebender();
        AncestralVision target = suspendedAncestralVision(1);
        turnFaceUp(timebender);

        chooseModeAndTarget("Put two time counters", target.getId());

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 3);
    }

    @Test
    void removingLastTimeCounterFromSuspendedCardOffersItsSuspendCast() {
        turnFaceUp();
        AncestralVision target = suspendedAncestralVision(1);

        chooseModeAndTarget("Remove two time counters", target.getId());

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void cannotTargetExiledCardWithNonSuspendTimeCounters() {
        turnFaceUp();
        Timebender target = new Timebender();
        harness.setExile(player1, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), 3);
        gd.exiledCardsWithNonSuspendTimeCounters.add(target.getId());

        harness.handleListChoice(player1, "Remove two time counters");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 3);
    }

    private Permanent turnFaceUp() {
        Permanent timebender = prepareFaceDownTimebender();
        turnFaceUp(timebender);
        return timebender;
    }

    private Permanent prepareFaceDownTimebender() {
        harness.setHand(player1, List.of(new Timebender()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent timebender = findPermanent(player1, "Timebender");
        return timebender;
    }

    private void turnFaceUp(Permanent timebender) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(timebender));
    }

    private AncestralVision suspendedAncestralVision(int timeCounters) {
        AncestralVision target = new AncestralVision();
        harness.setExile(player1, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), timeCounters);
        return target;
    }

    private void chooseModeAndTarget(String mode, java.util.UUID targetId) {
        harness.handleListChoice(player1, mode);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }
}
