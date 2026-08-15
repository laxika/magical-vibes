package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrovikanSorcerer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MidnightOilTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with seven hour counters")
    void entersWithSevenHourCounters() {
        harness.setHand(player1, List.of(new MidnightOil()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent oil = findPermanent(player1, "Midnight Oil");
        assertThat(oil.getCounterCount(CounterType.HOUR)).isEqualTo(7);
    }

    @Test
    @DisplayName("Draw-step trigger draws an additional card and removes two hour counters")
    void drawStepTriggerDrawsAndRemovesCounters() {
        Permanent oil = addOil(player1, 7);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 2);
        assertThat(oil.getCounterCount(CounterType.HOUR)).isEqualTo(5);
    }

    @Test
    @DisplayName("Maximum hand size follows the current number of hour counters")
    void maximumHandSizeFollowsHourCounters() {
        Permanent oil = addOil(player1, 3);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 19);
        assertThat(oil.getCounterCount(CounterType.HOUR)).isEqualTo(3);
    }

    @Test
    @DisplayName("Controller loses one life when a card is discarded")
    void controllerLosesLifeWhenDiscarding() {
        addOil(player1, 7);
        addCreatureReady(player1, new KrovikanSorcerer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
        harness.assertLife(player1, 19);
    }

    private Permanent addOil(Player player, int hourCounters) {
        Permanent oil = harness.addToBattlefieldAndReturn(player, new MidnightOil());
        oil.setCounterCount(CounterType.HOUR, hourCounters);
        return oil;
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
