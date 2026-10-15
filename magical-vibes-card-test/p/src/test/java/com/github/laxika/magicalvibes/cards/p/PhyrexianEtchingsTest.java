package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.cards.s.SurgingAether;
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

@CardUsed({PhyrexianEtchings.class, SurgingAether.class, SnowCoveredMountain.class})
class PhyrexianEtchingsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card for each age counter at the controller's end step")
    void drawsForAgeCountersAtEndStep() {
        Permanent etchings = harness.addToBattlefieldAndReturn(player1, new PhyrexianEtchings());
        etchings.setCounterCount(CounterType.AGE, 2);
        harness.setLibrary(player1, List.of(
                new SnowCoveredMountain(), new SnowCoveredMountain(), new SnowCoveredMountain()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Paying cumulative upkeep scales with age counters and keeps it on the battlefield")
    void payingCumulativeUpkeepKeepsItOnBattlefield() {
        Permanent etchings = harness.addToBattlefieldAndReturn(player1, new PhyrexianEtchings());
        etchings.setCounterCount(CounterType.AGE, 2);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(etchings.getCounterCount(CounterType.AGE)).isEqualTo(3);

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(etchings);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during its controller's upkeep")
    void cumulativeUpkeepTriggersOnlyDuringControllersUpkeep() {
        Permanent etchings = harness.addToBattlefieldAndReturn(player1, new PhyrexianEtchings());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(etchings.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(etchings);
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
        harness.setHand(player2, List.of(new SurgingAether()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, etchings.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(etchings);
        assertThat(gd.playerHands.get(player1.getId())).contains(etchings.getCard());
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("No age counters means no cards drawn at the controller's end step")
    void noAgeCountersDrawsNoCards() {
        harness.addToBattlefield(player1, new PhyrexianEtchings());
        harness.setLibrary(player1, List.of(new SnowCoveredMountain()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw at the opponent's end step")
    void opponentsEndStepDoesNotDraw() {
        Permanent etchings = harness.addToBattlefieldAndReturn(player1, new PhyrexianEtchings());
        etchings.setCounterCount(CounterType.AGE, 2);
        harness.setLibrary(player1, List.of(new SnowCoveredMountain(), new SnowCoveredMountain()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Draw count uses age counters when the end-step trigger resolves")
    void drawCountUsesCountersAtResolution() {
        Permanent etchings = harness.addToBattlefieldAndReturn(player1, new PhyrexianEtchings());
        etchings.setCounterCount(CounterType.AGE, 1);
        harness.setLibrary(player1, List.of(
                new SnowCoveredMountain(), new SnowCoveredMountain(), new SnowCoveredMountain()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        etchings.setCounterCount(CounterType.AGE, 3);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
    }

    @Test
    @DisplayName("Sacrificing an opponent-owned Etchings makes its last controller lose life")
    void lastControllerLosesLifeRatherThanOwner() {
        PhyrexianEtchings card = new PhyrexianEtchings();
        card.setOwnerId(player1.getId());
        Permanent etchings = harness.addToBattlefieldAndReturn(player2, card);
        etchings.setCounterCount(CounterType.AGE, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(etchings);
        harness.assertInGraveyard(player1, "Phyrexian Etchings");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }
}
