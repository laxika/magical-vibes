package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(IngeniousProdigy.class)
class IngeniousProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new IngeniousProdigy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ingenious Prodigy")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Accepting the upkeep ability removes a counter and draws a card")
    void acceptingUpkeepAbilityRemovesCounterAndDraws() {
        Permanent prodigy = addProdigyWithCounters(2);
        harness.setLibrary(player1, List.of(new IngeniousProdigy()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(prodigy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the upkeep ability leaves the counter and hand unchanged")
    void decliningUpkeepAbilityDoesNothing() {
        Permanent prodigy = addProdigyWithCounters(1);
        harness.setLibrary(player1, List.of(new IngeniousProdigy()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(prodigy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("The upkeep ability does not trigger without a +1/+1 counter")
    void doesNotTriggerWithoutCounters() {
        addProdigyWithCounters(0);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("The counter condition is checked again when the upkeep ability resolves")
    void checksCounterConditionAtResolution() {
        Permanent prodigy = addProdigyWithCounters(1);
        harness.setLibrary(player1, List.of(new IngeniousProdigy()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep();
        prodigy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addProdigyWithCounters(int counterCount) {
        Permanent prodigy = addCreatureReady(player1, new IngeniousProdigy());
        prodigy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counterCount);
        return prodigy;
    }

    private void advanceToUpkeep() {
        advanceToUpkeep(player1);
    }
}
