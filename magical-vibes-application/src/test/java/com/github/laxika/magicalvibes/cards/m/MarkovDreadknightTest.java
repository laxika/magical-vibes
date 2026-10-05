package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarkovDreadknight.class})
class MarkovDreadknightTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card puts two +1/+1 counters on this creature")
    void discardAddsTwoPlusOnePlusOneCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent dreadknight = harness.addToBattlefieldAndReturn(player1, new MarkovDreadknight());
        int basePower = gqs.getEffectivePower(gd, dreadknight);
        int baseToughness = gqs.getEffectiveToughness(gd, dreadknight);
        harness.setHand(player1, List.of(new MarkovDreadknight()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Markov Dreadknight");
        assertThat(gqs.getEffectivePower(gd, dreadknight)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, dreadknight)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new MarkovDreadknight());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void discardIsPaidBeforeCountersAreAdded() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent dreadknight = harness.addToBattlefieldAndReturn(player1, new MarkovDreadknight());
        harness.setHand(player1, List.of(new MarkovDreadknight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Markov Dreadknight");
        assertThat(dreadknight.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();

        harness.passBothPriorities();

        assertThat(dreadknight.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(2);
    }

    @Test
    void tappedSummoningSickCreatureCanActivateRepeatedly() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent dreadknight = harness.addToBattlefieldAndReturn(player1, new MarkovDreadknight());
        dreadknight.setTapped(true);
        dreadknight.setSummoningSick(true);
        harness.setHand(player1, List.of(new MarkovDreadknight(), new MarkovDreadknight()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(dreadknight.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent dreadknight = harness.addToBattlefieldAndReturn(player1, new MarkovDreadknight());
        harness.setHand(player1, List.of(new MarkovDreadknight()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Markov Dreadknight");
        harness.assertNotInGraveyard(player1, "Markov Dreadknight");
        assertThat(dreadknight.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
    }
}
