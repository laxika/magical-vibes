package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.WibblyWobblyTimeyWimey;
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

@CardUsed({FourKnocks.class, WibblyWobblyTimeyWimey.class})
class FourKnocksTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four time counters")
    void entersWithFourTimeCounters() {
        harness.setHand(player1, List.of(new FourKnocks()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Four Knocks").getCounterCount(CounterType.TIME))
                .isEqualTo(4);
    }

    @Test
    @DisplayName("Draws a card at the beginning of its controller's first main phase")
    void drawsAtBeginningOfFirstMainPhase() {
        addFourKnocks(player1);
        harness.setLibrary(player1, List.of(new FourKnocks(), new FourKnocks()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Removes one time counter during upkeep and sacrifices on the last one")
    void removesCountersAndSacrificesOnLastCounter() {
        Permanent fourKnocks = addFourKnocks(player1);
        fourKnocks.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Four Knocks");
        harness.assertInGraveyard(player1, "Four Knocks");
    }

    @Test
    @DisplayName("Does not draw during an opponent's first main phase")
    void doesNotTriggerOnOpponentsFirstMainPhase() {
        addFourKnocks(player1);
        harness.setLibrary(player1, List.of(new FourKnocks(), new FourKnocks()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToPrecombatMain(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removesOnlyOneCounterOnUpkeep() {
        Permanent fourKnocks = addFourKnocks(player1);
        fourKnocks.setCounterCount(CounterType.TIME, 4);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(fourKnocks.getCounterCount(CounterType.TIME)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Four Knocks");
    }

    @Test
    void doesNotRemoveCountersDuringOpponentsUpkeep() {
        Permanent fourKnocks = addFourKnocks(player1);
        fourKnocks.setCounterCount(CounterType.TIME, 4);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(fourKnocks.getCounterCount(CounterType.TIME)).isEqualTo(4);
    }

    @Test
    void doesNotTriggerUpkeepWithoutTimeCounters() {
        Permanent fourKnocks = addFourKnocks(player1);
        fourKnocks.setCounterCount(CounterType.TIME, 0);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Four Knocks");
    }

    @Test
    void lastCounterRemovalCreatesSeparateSacrificeTrigger() {
        Permanent fourKnocks = addFourKnocks(player1);
        fourKnocks.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(fourKnocks.getCounterCount(CounterType.TIME)).isZero();
        harness.assertOnBattlefield(player1, "Four Knocks");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Four Knocks");
        harness.assertInGraveyard(player1, "Four Knocks");
    }

    @Test
    void timeTravelRemovingLastCounterTriggersSacrifice() {
        Permanent fourKnocks = addFourKnocks(player1);
        fourKnocks.setCounterCount(CounterType.TIME, 1);
        harness.setLibrary(player1, List.of(new FourKnocks(), new FourKnocks()));
        harness.setHand(player1, List.of(new WibblyWobblyTimeyWimey()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Four Knocks");
        harness.assertInGraveyard(player1, "Four Knocks");
    }

    @Test
    void doesNotDrawAtBeginningOfPostcombatMainPhase() {
        addFourKnocks(player1);
        harness.setLibrary(player1, List.of(new FourKnocks(), new FourKnocks()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addFourKnocks(Player player) {
        return harness.addToBattlefieldAndReturn(player, new FourKnocks());
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
