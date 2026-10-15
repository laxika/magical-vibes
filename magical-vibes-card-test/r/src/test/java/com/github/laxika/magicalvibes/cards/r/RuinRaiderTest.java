package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinRaider.class, RaptorHatchling.class, Forest.class, EntrancingMelody.class})
class RuinRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("When raid met, reveals top card, puts it into hand, and loses life equal to mana value")
    void raidMetRevealsAndDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        Card topCard = new RaptorHatchling(); // MV 2
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setLife(player1, 20);

        markAttackedThisTurn();
        advanceToEndStep();

        // Resolve the raid trigger
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("When raid met, revealing a land (mana value 0) causes no life loss")
    void raidMetRevealingLandCausesNoLifeLoss() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        Card topCard = new Forest(); // MV 0
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setLife(player1, 20);

        markAttackedThisTurn();
        advanceToEndStep();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("When raid met, card is removed from the top of the library")
    void raidMetCardRemovedFromLibrary() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        Card topCard = new RaptorHatchling();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        markAttackedThisTurn();
        advanceToEndStep();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("When raid not met, no end step trigger fires")
    void raidNotMetNoTrigger() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).addFirst(new RaptorHatchling());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);

        // Do NOT mark attacked this turn
        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step even if controller attacked")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).addFirst(new RaptorHatchling());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);

        markAttackedThisTurn();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does nothing when library is empty and raid is met")
    void doesNothingWhenLibraryEmpty() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        markAttackedThisTurn();
        advanceToEndStep();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Raid counts an earlier attack by another creature before Ruin Raider entered")
    void triggersAfterAnotherCreatureAttackedBeforeEntering() {
        addCreatureReady(player1, new RaptorHatchling());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        Card topCard = new RaptorHatchling();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A queued raid ability still resolves after Ruin Raider leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        Card topCard = new RaptorHatchling();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        markAttackedThisTurn();
        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Each Ruin Raider reveals the current top card when its own ability resolves")
    void multipleRaidersRevealSuccessiveCards() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        Card firstCard = new RaptorHatchling();
        Card secondCard = new RuinRaider();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setLife(player1, 20);
        markAttackedThisTurn();
        advanceToEndStep();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard);
        harness.assertLife(player1, 18);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("X contributes zero to the revealed card's mana value")
    void revealingXSpellUsesManaValueOutsideStack() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        Card topCard = new EntrancingMelody();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        markAttackedThisTurn();

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An opponent's attack does not satisfy the controller's raid condition")
    void opponentsAttackDoesNotEnableRaid() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        Card topCard = new RaptorHatchling();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 20);
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
