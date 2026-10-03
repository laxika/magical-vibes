package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.FanningTheFlames;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrassInfiniscope.class, DarkRitual.class, FanningTheFlames.class})
class BrassInfiniscopeTest extends BaseCardTest {

    @Test
    @DisplayName("The next X spell draws a card and gains half X life rounded down")
    void nextXSpellDrawsAndGainsHalfXLife() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Card drawnCard = new DarkRitual();
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent scope = harness.addToBattlefieldAndReturn(player1, new BrassInfiniscope());
        harness.setHand(player1, List.of(new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(scope.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A non-X spell is ignored and the trigger is consumed once")
    void ignoresNonXSpellAndTriggersOnlyOnce() {
        Card firstDrawnCard = new DarkRitual();
        Card secondDrawnCard = new DarkRitual();
        harness.setLibrary(player1, List.of(firstDrawnCard, secondDrawnCard));
        harness.addToBattlefieldAndReturn(player1, new BrassInfiniscope());
        harness.setHand(player1, List.of(
                new DarkRitual(), new FanningTheFlames(), new FanningTheFlames()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        harness.castAndResolveInstant(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondDrawnCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    void zeroXStillDrawsAndConsumesTheTrigger() {
        Card drawnCard = new DarkRitual();
        Card remainingCard = new DarkRitual();
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));
        harness.addToBattlefield(player1, new BrassInfiniscope());
        harness.setHand(player1, List.of(new FanningTheFlames(), new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();
        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void opponentsXSpellDoesNotConsumeTheTrigger() {
        Card drawnCard = new DarkRitual();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new BrassInfiniscope());
        harness.setHand(player2, List.of(new FanningTheFlames()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, 4, player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, 4, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void independentActivationsBothTriggerEvenAfterSourcesLeave() {
        Card firstDrawnCard = new DarkRitual();
        Card secondDrawnCard = new DarkRitual();
        harness.setLibrary(player1, List.of(firstDrawnCard, secondDrawnCard));
        harness.addToBattlefield(player1, new BrassInfiniscope());
        harness.addToBattlefield(player1, new BrassInfiniscope());
        harness.setHand(player1, List.of(new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.castAndResolveSorcery(player1, 0, 4, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawnCard, secondDrawnCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void unusedTriggerExpiresAtEndOfTurn() {
        Card undrawnCard = new DarkRitual();
        harness.setLibrary(player1, List.of(undrawnCard));
        harness.setLibrary(player2, List.of(new DarkRitual(), new DarkRitual()));
        harness.addToBattlefield(player1, new BrassInfiniscope());
        harness.activateAbility(player1, 0, null, null);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, 4, player2.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawnCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
