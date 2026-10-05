package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaraudingLooter.class, Forest.class, Island.class, AncientBrontodon.class})
class MaraudingLooterTest extends BaseCardTest {


    @Test
    @DisplayName("When raid met and may accepted, draws a card then discards a card")
    void raidMetAcceptMayDrawsAndDiscards() {
        harness.addToBattlefield(player1, new MaraudingLooter());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of(new AncientBrontodon()));

        markAttackedThisTurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Advance to end step — raid trigger queues MayEffect onto stack
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);

        // Resolve the triggered ability — MayEffect presents the may choice
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Drew a card, now awaiting discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        // Discard a card
        harness.handleCardChosen(player1, 0);

        // Drew 1, discarded 1 — net hand size unchanged
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }


    @Test
    @DisplayName("When raid met and may declined, no draw or discard occurs")
    void raidMetDeclineMayNoDrawNoDiscard() {
        harness.addToBattlefield(player1, new MaraudingLooter());
        harness.setHand(player1, List.of(new AncientBrontodon()));

        markAttackedThisTurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Advance to end step — raid trigger queues MayEffect onto stack
        harness.passBothPriorities();

        // Resolve the triggered ability — MayEffect presents the may choice
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // Hand size unchanged — no draw, no discard
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }


    @Test
    @DisplayName("When raid not met (did not attack), end step trigger does not fire")
    void raidNotMetNoTrigger() {
        harness.addToBattlefield(player1, new MaraudingLooter());
        harness.setHand(player1, List.of(new AncientBrontodon()));

        // Do NOT mark attacked this turn
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        // No may ability prompt — raid condition not met
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        // Hand size unchanged
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }


    @Test
    @DisplayName("Does not trigger on opponent's end step even if controller attacked")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new MaraudingLooter());

        // Mark player1 attacked, but it's player2's turn
        markAttackedThisTurn();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        // No trigger for player1's Marauding Looter on player2's end step
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Declining the loot leaves the library, hand and graveyard unchanged")
    void decliningDoesNotDrawOrDiscard() {
        harness.addToBattlefield(player1, new MaraudingLooter());
        Forest top = new Forest();
        Island held = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(held));
        markAttackedThisTurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty hand still draws first and must discard the drawn card")
    void emptyHandDiscardsDrawnCard() {
        harness.addToBattlefield(player1, new MaraudingLooter());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn, new Island()));
        harness.setHand(player1, List.of());
        markAttackedThisTurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Raid counts an attack by another creature even when the Looter entered afterward")
    void anotherCreatureAttackingBeforeLooterEntersEnablesRaid() {
        addCreatureReady(player1, new AncientBrontodon());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.addToBattlefield(player1, new MaraudingLooter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("A triggered loot ability resolves after its source leaves the battlefield")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        MaraudingLooter looter = new MaraudingLooter();
        harness.addToBattlefield(player1, looter);
        Forest drawn = new Forest();
        Island held = new Island();
        harness.setLibrary(player1, List.of(drawn, new Island()));
        harness.setHand(player1, List.of(held));
        markAttackedThisTurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerGraveyards.get(player1.getId()).add(looter);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held, drawn);
        harness.handleCardChosen(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(looter, drawn);
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

}
