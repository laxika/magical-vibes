package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianEtchingsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card for each age counter at the controller's end step")
    void drawsForAgeCountersAtEndStep() {
        Permanent etchings = harness.addToBattlefieldAndReturn(player1, new PhyrexianEtchings());
        etchings.setCounterCount(CounterType.AGE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices it and loses twice its age counters in life")
    void decliningUpkeepLosesLifeForAgeCounters() {
        Permanent etchings = harness.addToBattlefieldAndReturn(player1, new PhyrexianEtchings());
        etchings.setCounterCount(CounterType.AGE, 2);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(etchings.getCounterCount(CounterType.AGE)).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(etchings);
        harness.assertInGraveyard(player1, "Phyrexian Etchings");
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Returning it to hand does not trigger its graveyard ability")
    void returningToHandDoesNotLoseLife() {
        Permanent etchings = harness.addToBattlefieldAndReturn(player1, new PhyrexianEtchings());
        etchings.setCounterCount(CounterType.AGE, 2);
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, etchings.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(etchings);
        assertThat(gd.playerHands.get(player1.getId())).contains(etchings.getCard());
        harness.assertLife(player1, 20);
    }
}
