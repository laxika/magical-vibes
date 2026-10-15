package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({Fasting.class, GrizzlyBears.class, HowlingMine.class})
class FastingTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep puts a hunger counter on Fasting")
    void upkeepAddsHungerCounter() {
        Permanent fasting = addFasting();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(fasting.getCounterCount(CounterType.HUNGER)).isEqualTo(1);
    }

    @Test
    @DisplayName("The fifth hunger counter destroys Fasting")
    void fifthHungerCounterDestroysFasting() {
        Permanent fasting = addFasting();
        fasting.setCounterCount(CounterType.HUNGER, 4);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fasting");
        harness.assertInGraveyard(player1, "Fasting");
    }

    @Test
    @DisplayName("Fasting does not trigger during an opponent's upkeep")
    void opponentUpkeepDoesNotAddHungerCounter() {
        Permanent fasting = addFasting();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(fasting.getCounterCount(CounterType.HUNGER)).isZero();
        harness.assertOnBattlefield(player1, "Fasting");
    }

    @Test
    @DisplayName("Skipping the draw step gains 2 life and skips draw-step triggers")
    void skippingDrawStepGainsLifeAndSkipsDrawStepTriggers() {
        addFasting();
        harness.addToBattlefield(player1, new HowlingMine());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        beginDrawStep(true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Fasting");
    }

    @Test
    @DisplayName("Declining the draw-step replacement draws a card and destroys Fasting")
    void decliningDrawStepReplacementDrawsAndDestroysFasting() {
        addFasting();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        beginDrawStep(false);
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Fasting");
        harness.assertInGraveyard(player1, "Fasting");
    }

    @Test
    @DisplayName("An opponent drawing a card does not destroy Fasting")
    void opponentDrawDoesNotDestroyFasting() {
        addFasting();
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        advanceToDrawStep(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
        harness.assertOnBattlefield(player1, "Fasting");
    }

    @Test
    @DisplayName("The fourth hunger counter does not destroy Fasting")
    void fourthHungerCounterDoesNotDestroyFasting() {
        Permanent fasting = addFasting();
        fasting.setCounterCount(CounterType.HUNGER, 3);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(fasting.getCounterCount(CounterType.HUNGER)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Fasting");
    }

    @Test
    @DisplayName("Fasting with more than five hunger counters is destroyed during upkeep")
    void moreThanFiveHungerCountersDestroysFasting() {
        Permanent fasting = addFasting();
        fasting.setCounterCount(CounterType.HUNGER, 6);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fasting");
        harness.assertInGraveyard(player1, "Fasting");
    }

    @Test
    @DisplayName("Drawing outside the draw step triggers destruction on the stack")
    void drawingOutsideDrawStepDestroysFastingOnResolution() {
        addFasting();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Fasting");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fasting");
        harness.assertInGraveyard(player1, "Fasting");
    }

    @Test
    @DisplayName("Skipping the draw step proceeds directly to the main phase")
    void skippingDrawStepDoesNotLeaveDrawStepPriority() {
        addFasting();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.withAutoStop(TurnStep.DRAW, () ->
                harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> beginDrawStep(true)));

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        harness.assertOnBattlefield(player1, "Fasting");
    }

    @Test
    @DisplayName("Declining one Fasting still allows another Fasting to skip the draw step")
    void decliningFirstFastingStillOffersSecondFasting() {
        addFasting();
        addFasting();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        advanceToDrawStep(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Multiple copies of Fasting only gain life once for a skipped draw step")
    void multipleCopiesGainOnlyTwoLifeForSkipping() {
        addFasting();
        addFasting();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        beginDrawStep(true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    private Permanent addFasting() {
        return harness.addToBattlefieldAndReturn(player1, new Fasting());
    }

    private void beginDrawStep(boolean skip) {
        advanceToDrawStep(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, skip);
    }

    private void advanceToDrawStep(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
        resolveAllTriggers();

        harness.passUntil(activePlayer, TurnStep.DRAW);
    }
}
