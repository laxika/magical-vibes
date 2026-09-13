package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoShintaiOfBoundlessVigor.class, HondenOfSeeingWinds.class, GrizzlyBears.class})
class GoShintaiOfBoundlessVigorTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} puts a +1/+1 counter on a target Shrine for each Shrine you control")
    void paysToPutCountersOnTargetShrine() {
        harness.addToBattlefield(player1, new GoShintaiOfBoundlessVigor());
        Permanent honden = harness.addToBattlefieldAndReturn(player1, new HondenOfSeeingWinds());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, honden.getId());

        assertThat(honden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the payment puts no counters on a Shrine")
    void declinesPayment() {
        Permanent honden = harness.addToBattlefieldAndReturn(player1, new HondenOfSeeingWinds());
        harness.addToBattlefield(player1, new GoShintaiOfBoundlessVigor());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(honden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Only Shrines are legal targets")
    void onlyShrinesCanBeTargeted() {
        harness.addToBattlefield(player1, new GoShintaiOfBoundlessVigor());
        Permanent honden = harness.addToBattlefieldAndReturn(player1, new HondenOfSeeingWinds());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(
                        findPermanent(player1, "Go-Shintai of Boundless Vigor").getId(),
                        honden.getId())
                .doesNotContain(bears.getId());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
