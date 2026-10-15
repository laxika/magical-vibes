package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverlordOfTheHauntwoods.class})
class OverlordOfTheHauntwoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a tapped Everywhere land token")
    void enteringCreatesEverywhere() {
        harness.setHand(player1, List.of(new OverlordOfTheHauntwoods()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent everywhere = findPermanent(player1, "Everywhere");
        assertThat(everywhere.isTapped()).isTrue();
        assertThat(everywhere.getCard().getType()).isEqualTo(CardType.LAND);
        assertThat(everywhere.getCard().getColor()).isNull();
        assertThat(everywhere.getCard().getSubtypes()).containsExactlyInAnyOrder(
                CardSubtype.PLAINS,
                CardSubtype.ISLAND,
                CardSubtype.SWAMP,
                CardSubtype.MOUNTAIN,
                CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Casting with impending enters with four time counters and is not a creature")
    void impendingCastEntersWithCountersAndIsNotCreature() {
        Permanent overlord = castWithImpending();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    @Test
    @DisplayName("Removing the last impending counter makes it a creature")
    void lastCounterMakesItCreature() {
        Permanent overlord = castWithImpending();
        overlord.setCounterCount(CounterType.TIME, 1);

        advanceToOwnEndStep();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gqs.isCreature(gd, overlord)).isTrue();
    }

    @Test
    @DisplayName("Attacking creates another Everywhere land token")
    void attackingCreatesEverywhere() {
        Permanent overlord = addCreatureReady(player1, new OverlordOfTheHauntwoods());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Everywhere")).hasSize(1);
        assertThat(overlord.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Normal casting enters as a creature without impending counters")
    void normalCastDoesNotUseImpending() {
        harness.setHand(player1, List.of(new OverlordOfTheHauntwoods()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent overlord = findPermanent(player1, "Overlord of the Hauntwoods");
        assertThat(overlord.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gqs.isCreature(gd, overlord)).isTrue();

        overlord.setCounterCount(CounterType.TIME, 2);
        advanceToOwnEndStep();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, overlord)).isTrue();
    }

    @Test
    @DisplayName("Impending still creates a tapped Everywhere token on entry")
    void impendingEntryCreatesToken() {
        castWithImpending();

        assertThat(findPermanents(player1, "Everywhere")).hasSize(1);
        assertThat(findPermanent(player1, "Everywhere").isTapped()).isTrue();
        assertThat(findPermanents(player2, "Everywhere")).isEmpty();
    }

    @Test
    @DisplayName("Impending removes exactly one counter during the controller's end step")
    void ownEndStepRemovesOneCounter() {
        Permanent overlord = castWithImpending();

        advanceToOwnEndStep();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
        assertThat(findPermanents(player1, "Everywhere")).hasSize(1);
    }

    @Test
    @DisplayName("Impending does not remove counters during the opponent's end step")
    void opponentEndStepDoesNotRemoveCounter() {
        Permanent overlord = castWithImpending();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    @Test
    @DisplayName("Impending counters are present before the entry trigger resolves")
    void impendingCountersAreAppliedOnEntry() {
        harness.setHand(player1, List.of(new OverlordOfTheHauntwoods()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent overlord = findPermanent(player1, "Overlord of the Hauntwoods");
        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
        assertThat(findPermanents(player1, "Everywhere")).isEmpty();

        resolveAllTriggers();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(4);
        assertThat(findPermanents(player1, "Everywhere")).hasSize(1);
    }

    @Test
    @DisplayName("Adding a time counter after impending expires makes it a noncreature again")
    void newTimeCounterRestoresImpendingRestriction() {
        Permanent overlord = castWithImpending();
        overlord.setCounterCount(CounterType.TIME, 1);
        advanceToOwnEndStep();
        assertThat(gqs.isCreature(gd, overlord)).isTrue();

        overlord.setCounterCount(CounterType.TIME, 1);

        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    private Permanent castWithImpending() {
        harness.setHand(player1, List.of(new OverlordOfTheHauntwoods()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        return findPermanent(player1, "Overlord of the Hauntwoods");
    }

    private void advanceToOwnEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
