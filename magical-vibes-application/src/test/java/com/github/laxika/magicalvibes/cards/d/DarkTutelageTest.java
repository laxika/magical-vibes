package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndLoseLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarkTutelageTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Dark Tutelage has upkeep trigger effect")
    void hasCorrectProperties() {
        DarkTutelage card = new DarkTutelage();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(RevealTopCardPutIntoHandAndLoseLifeEffect.class);
    }

    // ===== Triggering =====

    @Test
    @DisplayName("Reveals top card, puts it into hand, and loses life equal to mana value")
    void revealsAndPutsIntoHandAndLosesLife() {
        harness.addToBattlefield(player1, new DarkTutelage());
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears(); // MV 2
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Revealing a land (mana value 0) causes no life loss")
    void revealingLandCausesNoLifeLoss() {
        harness.addToBattlefield(player1, new DarkTutelage());
        harness.setHand(player1, List.of());
        Card topCard = new Forest(); // MV 0
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new DarkTutelage());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Card is removed from the top of the library")
    void cardIsRemovedFromLibrary() {
        harness.addToBattlefield(player1, new DarkTutelage());
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Does nothing when library is empty")
    void doesNothingWhenLibraryEmpty() {
        harness.addToBattlefield(player1, new DarkTutelage());
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).clear();
        harness.setLife(player1, 20);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
