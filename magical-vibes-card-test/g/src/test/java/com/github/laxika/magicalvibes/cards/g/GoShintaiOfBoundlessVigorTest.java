package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.cards.m.MarchOfOtherworldlyLight;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoShintaiOfBoundlessVigor.class, HondenOfSeeingWinds.class, GrizzlyBears.class,
        MarchOfOtherworldlyLight.class})
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

    @Test
    @DisplayName("Payment creates a separate reflexive trigger before counters are placed")
    void paymentCreatesSeparateTrigger() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new GoShintaiOfBoundlessVigor());

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            advanceToEndStep(player1);
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.interaction.isAwaitingInput()).isFalse();

            harness.passBothPriorities();
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, shrine.getId());

            assertThat(shrine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            assertThat(shrine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        });
    }

    @Test
    @CardUsed({GoShintaiOfBoundlessVigor.class, HondenOfSeeingWinds.class, MarchOfOtherworldlyLight.class})
    @DisplayName("Removing a Shrine in response changes the count when the reflexive trigger resolves")
    void countsShrinesWhenReflexiveTriggerResolves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoShintaiOfBoundlessVigor());
        Permanent honden = harness.addToBattlefieldAndReturn(player1, new HondenOfSeeingWinds());
        harness.setHand(player2, List.of(new MarchOfOtherworldlyLight()));

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            advanceToEndStep(player1);
            harness.passBothPriorities();
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, honden.getId());

            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 2);
            harness.castInstantForXWithDiscards(player2, 0, 2, List.of(source.getId()), List.of());
            harness.passBothPriorities();
            harness.assertNotOnBattlefield(player1, "Go-Shintai of Boundless Vigor");
            harness.passBothPriorities();

            assertThat(honden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("An opponent's noncreature Shrine is legal but is not counted")
    void targetsOpponentShrineWithoutCountingIt() {
        harness.addToBattlefield(player1, new GoShintaiOfBoundlessVigor());
        Permanent honden = harness.addToBattlefieldAndReturn(player2, new HondenOfSeeingWinds());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, honden.getId());
        harness.passBothPriorities();

        assertThat(honden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting without enough mana does not place counters")
    void cannotPayWithoutMana() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new GoShintaiOfBoundlessVigor());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(shrine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new GoShintaiOfBoundlessVigor());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(shrine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
