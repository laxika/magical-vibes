package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.d.Delay;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Timebender.class, AncestralVision.class, Delay.class})
class TimebenderTest extends BaseCardTest {

    @Test
    @DisplayName("Turning face up removes two time counters from target permanent")
    void turningFaceUpRemovesTwoTimeCountersFromTargetPermanent() {
        turnFaceUp();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Timebender());
        target.setCounterCount(CounterType.TIME, 3);

        chooseModeAndTarget("Remove two time counters", target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Turning face up adds two time counters to target permanent with time counter")
    void turningFaceUpAddsTwoTimeCountersToTargetPermanentWithTimeCounter() {
        Permanent timebender = prepareFaceDownTimebender();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Timebender());
        target.setCounterCount(CounterType.TIME, 1);
        turnFaceUp(timebender);

        chooseModeAndTarget("Put two time counters", target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    @DisplayName("Turning face up can remove two time counters from suspended card")
    void turningFaceUpCanRemoveTwoTimeCountersFromSuspendedCard() {
        turnFaceUp();
        AncestralVision target = suspendedAncestralVision(3);

        chooseModeAndTarget("Remove two time counters", target.getId());

        assertThat(gd.exiledCardsWithCounter(CounterType.TIME)).containsEntry(target.getId(), 1);
    }

    @Test
    @DisplayName("Turning face up adds two time counters to suspended card")
    void turningFaceUpAddsTwoTimeCountersToSuspendedCard() {
        Permanent timebender = prepareFaceDownTimebender();
        AncestralVision target = suspendedAncestralVision(1);
        turnFaceUp(timebender);

        chooseModeAndTarget("Put two time counters", target.getId());

        assertThat(gd.exiledCardsWithCounter(CounterType.TIME)).containsEntry(target.getId(), 3);
    }

    @Test
    @DisplayName("Removing last time counter from suspended card offers its suspend cast")
    void removingLastTimeCounterFromSuspendedCardOffersItsSuspendCast() {
        turnFaceUp();
        AncestralVision target = suspendedAncestralVision(1);

        chooseModeAndTarget("Remove two time counters", target.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCardsWithCounter(CounterType.TIME)).doesNotContainKey(target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Cannot target exiled card with non suspend time counters")
    void cannotTargetExiledCardWithNonSuspendTimeCounters() {
        turnFaceUp();
        Timebender target = new Timebender();
        harness.setExile(player1, List.of(target));
        gd.setExiledCardCounters(target.getId(), CounterType.TIME, 3);
        gd.exiledCardsWithNonSuspendTimeCounters.add(target.getId());

        harness.handleListChoice(player1, "Remove two time counters");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCardsWithCounter(CounterType.TIME)).containsEntry(target.getId(), 3);
    }

    private Permanent turnFaceUp() {
        Permanent timebender = prepareFaceDownTimebender();
        turnFaceUp(timebender);
        return timebender;
    }

    @Test
    @DisplayName("Remove mode can target permanent without time counters")
    void removeModeCanTargetPermanentWithoutTimeCounters() {
        Permanent timebender = prepareFaceDownTimebender();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Timebender());
        turnFaceUp(timebender);

        chooseModeAndTarget("Remove two time counters", target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
        harness.assertOnBattlefield(player2, "Timebender");
    }

    @Test
    @DisplayName("Remove mode removes only available time counter")
    void removeModeRemovesOnlyAvailableTimeCounter() {
        Permanent timebender = prepareFaceDownTimebender();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Timebender());
        target.setCounterCount(CounterType.TIME, 1);
        turnFaceUp(timebender);

        chooseModeAndTarget("Remove two time counters", target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    @DisplayName("Add mode cannot target permanent without time counter")
    void addModeCannotTargetPermanentWithoutTimeCounter() {
        Permanent timebender = prepareFaceDownTimebender();
        timebender.setCounterCount(CounterType.TIME, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Timebender());
        turnFaceUp(timebender);
        harness.handleListChoice(player1, "Put two time counters");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    @DisplayName("Add mode does not restore counters if target loses last counter before resolution")
    void addModeDoesNotRestoreCountersIfTargetLosesLastCounterBeforeResolution() {
        Permanent timebender = prepareFaceDownTimebender();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Timebender());
        target.setCounterCount(CounterType.TIME, 1);
        turnFaceUp(timebender);
        harness.handleListChoice(player1, "Put two time counters");
        harness.handlePermanentChosen(player1, target.getId());
        target.setCounterCount(CounterType.TIME, 0);

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    @DisplayName("Remove mode can target card granted suspend by delay")
    void removeModeCanTargetCardGrantedSuspendByDelay() {
        Permanent timebender = prepareFaceDownTimebender();
        Timebender target = suspendTimebenderWithDelay();
        turnFaceUp(timebender);

        chooseModeAndTarget("Remove two time counters", target.getId());

        assertThat(gd.exiledCardsWithCounter(CounterType.TIME)).containsExactly(
                Map.entry(target.getId(), 1));
    }

    @Test
    @DisplayName("Add mode can target card granted suspend by delay")
    void addModeCanTargetCardGrantedSuspendByDelay() {
        Permanent timebender = prepareFaceDownTimebender();
        Timebender target = suspendTimebenderWithDelay();
        turnFaceUp(timebender);

        chooseModeAndTarget("Put two time counters", target.getId());

        assertThat(gd.exiledCardsWithCounter(CounterType.TIME)).containsExactly(
                Map.entry(target.getId(), 5));
    }

    private Timebender suspendTimebenderWithDelay() {
        Timebender target = new Timebender();
        harness.castFromHand(player1, target, "{U}");
        harness.setHand(player2, List.of(new Delay()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        return target;
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
        gd.setExiledCardCounters(target.getId(), CounterType.TIME, timeCounters);
        return target;
    }

    private void chooseModeAndTarget(String mode, java.util.UUID targetId) {
        harness.handleListChoice(player1, mode);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }
}
