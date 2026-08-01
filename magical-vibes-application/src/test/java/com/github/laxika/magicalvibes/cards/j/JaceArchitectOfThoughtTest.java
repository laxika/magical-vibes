package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingEachPlayerLibraryExile;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JaceArchitectOfThoughtTest extends BaseCardTest {

    @Test
    @DisplayName("+1: a creature an opponent controls that attacks gets -1/-0")
    void plusOneShrinksOpposingAttacker() {
        addReadyJace(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        activatePlusOne();

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities(); // resolve the delayed trigger

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("+1: your own attacking creatures are unaffected")
    void plusOneLeavesOwnAttackersAlone() {
        addReadyJace(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        activatePlusOne();

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("+1: the debuff stops applying once your next turn has begun")
    void plusOneWearsOffAtYourNextTurn() {
        addReadyJace(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        activatePlusOne();

        endTurn(player1); // -> player2's turn, the effect is still active
        endTurn(player2); // -> player1's next turn, the effect expires here
        endTurn(player1); // -> player2's turn again

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("−2: the chosen pile goes to hand and the other to the bottom of the library")
    void minusTwoChosenPileToHandOtherToBottom() {
        Card shock = new Shock();
        Card giantGrowth = new GiantGrowth();
        Card bears = new GrizzlyBears();
        addReadyJace(player1);
        harness.setLibrary(player1, List.of(shock, giantGrowth, bears));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // The opponent separates: Pile 1 = Shock, Pile 2 = Giant Growth + Grizzly Bears.
        harness.handleMultipleCardsChosen(player2, List.of(shock.getId()));
        // The controller takes Pile 1.
        harness.handleMayAbilityChosen(player1, true);
        // Two cards go to the bottom, so their order is chosen: Giant Growth first.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(giantGrowth, bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(giantGrowth, bears);
    }

    @Test
    @DisplayName("−2: declining takes the other pile and a single leftover card needs no ordering")
    void minusTwoDecliningTakesOtherPile() {
        Card shock = new Shock();
        Card giantGrowth = new GiantGrowth();
        Card bears = new GrizzlyBears();
        addReadyJace(player1);
        harness.setLibrary(player1, List.of(shock, giantGrowth, bears));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Pile 1 = Shock, Pile 2 = Giant Growth + Grizzly Bears.
        harness.handleMultipleCardsChosen(player2, List.of(shock.getId()));
        // Declining takes Pile 2; Pile 1's single card goes to the bottom with no ordering prompt.
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(giantGrowth, bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class)).isNull();
    }

    @Test
    @DisplayName("−8: exiles a nonland card from every library and offers them for free casting")
    void minusEightExilesFromEveryLibraryAndOffersFreeCasts() {
        Card ownShock = new Shock();
        Card opponentBears = new GrizzlyBears();
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 8);
        harness.setLibrary(player1, List.of(ownShock));
        harness.setLibrary(player2, List.of(opponentBears));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        // The controller searches their own library first (they are the active player), then the opponent's.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.exiledCards.stream().map(e -> e.card().getName()).toList())
                .containsExactlyInAnyOrder("Shock", "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingEachPlayerLibraryExile.class)).isFalse();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ImprovisationCapstoneCastChoice.class);
    }

    @Test
    @DisplayName("−8: the exiled opponent's creature can be cast without paying its mana cost")
    void minusEightCastsExiledCardForFree() {
        Card ownShock = new Shock();
        Card opponentBears = new GrizzlyBears();
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 8);
        harness.setLibrary(player1, List.of(ownShock));
        harness.setLibrary(player2, List.of(opponentBears));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Cast only the Grizzly Bears — no mana was added, so it can only resolve if it is free.
        harness.handleMultipleCardsChosen(player1, List.of(opponentBears.getId()));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("−8: a library with no nonland card is simply shuffled and skipped")
    void minusEightSkipsLibraryWithoutNonlandCards() {
        Card ownShock = new Shock();
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 8);
        harness.setLibrary(player1, List.of(ownShock));
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.exiledCards.stream().map(e -> e.card().getName()).toList())
                .containsExactly("Shock");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ImprovisationCapstoneCastChoice.class);
    }

    private void activatePlusOne() {
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    /**
     * Passes from {@code activePlayer}'s postcombat main through cleanup into the next player's turn.
     * Their hand is emptied first so the cleanup step never stops for a discard-to-hand-size choice.
     */
    private void endTurn(Player activePlayer) {
        harness.setHand(activePlayer, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        for (int step = 0; step < 10 && activePlayer.getId().equals(gd.activePlayerId); step++) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }

    private Permanent addReadyJace(Player player) {
        Permanent perm = new Permanent(new JaceArchitectOfThought());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
