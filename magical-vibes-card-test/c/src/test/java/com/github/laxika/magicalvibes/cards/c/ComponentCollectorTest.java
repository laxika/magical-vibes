package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.u.UnblinkingObserver;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ComponentCollector.class, Forest.class, UnblinkingObserver.class})
class ComponentCollectorTest extends BaseCardTest {

    @Test
    void becomesDayAsItEntersWhenThereIsNoDesignation() {
        harness.castFromHand(player1, new ComponentCollector(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void dayNightChangeMayTapTargetNonlandPermanent() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new ComponentCollector());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnblinkingObserver());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        advanceToNextTurn();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(land.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void decliningMayLeavesTargetUntapped() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new ComponentCollector());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnblinkingObserver());
        advanceToNextTurn();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void dayNightChangeMayUntapTargetNonlandPermanent() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new ComponentCollector());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnblinkingObserver());
        advanceToNextTurn();
        target.tap();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void enteringDuringDayDoesNotTriggerOrChangeDesignation() {
        gd.dayNight = DayNight.DAY;

        harness.castFromHand(player1, new ComponentCollector(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void enteringDuringNightDoesNotMakeItDayOrTrigger() {
        gd.dayNight = DayNight.NIGHT;

        harness.castFromHand(player1, new ComponentCollector(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void dayRemainsDayWhenPreviousActivePlayerCastOneSpell() {
        gd.dayNight = DayNight.DAY;
        harness.castFromHand(player1, new ComponentCollector(), "{2}{U}");
        harness.passBothPriorities();

        advanceToNextTurn();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void nightBecomesDayAfterPreviousActivePlayerCastTwoSpells() {
        gd.dayNight = DayNight.NIGHT;
        harness.addToBattlefield(player1, new ComponentCollector());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnblinkingObserver());
        harness.castFromHand(player1, new UnblinkingObserver(), "{1}{U}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new UnblinkingObserver(), "{1}{U}");
        harness.passBothPriorities();

        advanceToNextTurn();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(target.isTapped()).isTrue();
    }

    private void advanceToNextTurn() {
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.UPKEEP));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.UPKEEP));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
