package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChimneyImpTest extends BaseCardTest {

    @Test
    @DisplayName("When Chimney Imp dies, its controller chooses an opponent")
    void deathTriggerTargetsOpponent() {
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new ChimneyImp());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        killImp(imp);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Target opponent chooses a hand card to put on top of their library")
    void putsChosenCardOnTopOfOpponentsLibrary() {
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new ChimneyImp());
        List<Card> hand = new ArrayList<>(List.of(new GrizzlyBears(), new Peek()));
        harness.setHand(player2, hand);
        Card oldTop = gd.playerDecks.get(player2.getId()).getFirst();

        killImp(imp);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.playerId()).isEqualTo(player2.getId());
                    assertThat(choice.maxCount()).isEqualTo(1);
                });

        harness.handleMultipleCardsChosen(player2, List.of(hand.getFirst().getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(hand.get(1));
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(hand.getFirst(), oldTop);
    }

    @Test
    @DisplayName("An opponent with an empty hand does not get a card choice")
    void emptyHandDoesNothing() {
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new ChimneyImp());
        harness.setHand(player2, List.of());

        killImp(imp);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void killImp(Permanent imp) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, new ArrayList<>(List.of(new Murder())));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, imp.getId());
        harness.passBothPriorities();
    }
}
