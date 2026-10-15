package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ClockworkDroid;
import com.github.laxika.magicalvibes.cards.d.DinosaursOnASpaceship;
import com.github.laxika.magicalvibes.cards.f.FleshDuplicate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WibblyWobblyTimeyWimey.class, ClockworkDroid.class, FleshDuplicate.class,
        DinosaursOnASpaceship.class})
class WibblyWobblyTimeyWimeyTest extends BaseCardTest {

    @Test
    void timeTravelsThenDrawsACard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        target.setCounterCount(CounterType.TIME, 1);
        ClockworkDroid drawnCard = new ClockworkDroid();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new WibblyWobblyTimeyWimey(), "{1}{U}");
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleListChoice(player1, "ADD");

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void drawsWhenThereAreNoEligibleObjects() {
        Permanent withoutTime = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        opposing.setCounterCount(CounterType.TIME, 2);
        ClockworkDroid drawnCard = new ClockworkDroid();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new WibblyWobblyTimeyWimey(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(withoutTime.getCounterCount(CounterType.TIME)).isZero();
        assertThat(opposing.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void canDeclineTimeTravelAndStillDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        target.setCounterCount(CounterType.TIME, 1);
        ClockworkDroid drawnCard = new ClockworkDroid();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new WibblyWobblyTimeyWimey(), "{1}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "SKIP");

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void makesIndependentChoicesAndDrawsOnlyAfterAllChoices() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        first.setCounterCount(CounterType.TIME, 1);
        second.setCounterCount(CounterType.TIME, 2);
        ClockworkDroid drawnCard = new ClockworkDroid();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new WibblyWobblyTimeyWimey(), "{1}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");
        assertThat(first.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleListChoice(player1, "ADD");

        assertThat(second.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
    @Test
    void removingLastTimeCounterTriggersVanishingAfterDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        FleshDuplicate duplicateCard = new FleshDuplicate();
        harness.castFromHand(player1, duplicateCard, "{U}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        Permanent duplicate = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == duplicateCard)
                .findFirst().orElseThrow();
        duplicate.setCounterCount(CounterType.TIME, 1);
        ClockworkDroid drawnCard = new ClockworkDroid();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new WibblyWobblyTimeyWimey(), "{1}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");

        assertThat(duplicate.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(duplicate);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(duplicateCard);
    }

    @Test
    void removesCounterFromSuspendedCardAndPreservesItsRemovalTrigger() {
        DinosaursOnASpaceship suspended = new DinosaursOnASpaceship();
        harness.setHand(player1, List.of(suspended));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateHandAbility(player1, 0, null);
        ClockworkDroid drawnCard = new ClockworkDroid();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new WibblyWobblyTimeyWimey(), "{1}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");

        assertThat(gd.exiledCardTimeCounters).containsEntry(suspended.getId(), 3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().isToken()).isTrue();
    }
}
