package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.a.Afterlife;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FractalTender.class, Concentrate.class, Afterlife.class, MentorOfTheMeek.class})
class FractalTenderTest extends BaseCardTest {

    @Test
    @DisplayName("Increment creates a 3/3 Fractal with three +1/+1 counters at the end step")
    void createsFractalAfterCounterWasPutOnIt() {
        Permanent tender = harness.addToBattlefieldAndReturn(player1, new FractalTender());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Concentrate()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(tender.getPlusOnePlusOneCounters()).isEqualTo(1);

        advanceToEndStepAndResolve(player1);

        List<Permanent> fractals = findPermanents(player1, "Fractal");
        assertThat(fractals).hasSize(1);
        Permanent fractal = fractals.getFirst();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(fractal.getEffectivePower()).isEqualTo(3);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not create a Fractal when no counter was put on it this turn")
    void doesNotCreateFractalWithoutCounterPlacement() {
        harness.addToBattlefield(player1, new FractalTender());

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Fractal")).isEmpty();
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Fractal enters as a 0/0 before receiving counters and triggers Mentor of the Meek")
    void fractalTriggersMentorBeforeCountersArePutOnIt() {
        harness.addToBattlefield(player1, new FractalTender());
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Concentrate()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("End-step ability creates a Fractal even if Tender is destroyed in response")
    void createsFractalAfterSourceLeavesBattlefield() {
        Permanent tender = harness.addToBattlefieldAndReturn(player1, new FractalTender());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Concentrate()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new Afterlife()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0, tender.getId());
        harness.assertInGraveyard(player1, "Fractal Tender");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
    }

    @Test
    @DisplayName("Increment does not trigger when mana spent equals both power and toughness")
    void incrementRequiresStrictlyGreaterManaSpent() {
        Permanent tender = harness.addToBattlefieldAndReturn(player1, new FractalTender());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Concentrate(), new Concentrate()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(tender.getPlusOnePlusOneCounters()).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(tender.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when its payment is declined")
    void wardCountersSpellWhenPaymentIsDeclined() {
        Permanent tender = harness.addToBattlefieldAndReturn(player1, new FractalTender());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Afterlife()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castInstant(player2, 0, tender.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Fractal Tender");
        harness.assertInGraveyard(player2, "Afterlife");
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Paying Ward allows an opponent's spell to resolve")
    void wardPaymentAllowsSpellToResolve() {
        Permanent tender = harness.addToBattlefieldAndReturn(player1, new FractalTender());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Afterlife()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castInstant(player2, 0, tender.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fractal Tender");
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }
}
