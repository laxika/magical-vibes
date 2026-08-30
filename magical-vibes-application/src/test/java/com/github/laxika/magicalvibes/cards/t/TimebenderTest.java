package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Timebender.class, GrizzlyBears.class, AncestralVision.class})
class TimebenderTest extends BaseCardTest {

    @Test
    void turningFaceUpRemovesTwoTimeCountersFromTargetPermanent() {
        Permanent timebender = turnFaceUp();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.TIME, 3);

        chooseModeAndTarget(timebender, "Remove two time counters", target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void turningFaceUpAddsTwoTimeCountersToTargetPermanentWithTimeCounter() {
        Permanent timebender = prepareFaceDownTimebender();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.TIME, 1);
        turnFaceUp(timebender);

        chooseModeAndTarget(timebender, "Put two time counters", target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    void turningFaceUpCanRemoveTwoTimeCountersFromSuspendedCard() {
        Permanent timebender = turnFaceUp();
        AncestralVision target = new AncestralVision();
        harness.setExile(player1, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), 3);

        chooseModeAndTarget(timebender, "Remove two time counters", target.getId());

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 1);
    }

    @Test
    void removingLastTimeCounterFromSuspendedCardOffersItsSuspendCast() {
        Permanent timebender = turnFaceUp();
        AncestralVision target = new AncestralVision();
        harness.setExile(player1, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), 1);

        chooseModeAndTarget(timebender, "Remove two time counters", target.getId());

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
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

    private void chooseModeAndTarget(Permanent timebender, String mode, java.util.UUID targetId) {
        harness.handleListChoice(player1, mode);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }
}
