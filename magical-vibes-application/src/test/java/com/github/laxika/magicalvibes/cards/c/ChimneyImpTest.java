package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElectrostaticBolt;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ChimneyImp.class, ElectrostaticBolt.class})
class ChimneyImpTest extends BaseCardTest {

    @Test
    @DisplayName("When Chimney Imp dies, its controller chooses an opponent")
    void deathTriggerTargetsOpponent() {
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new ChimneyImp());
        killImp(imp);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("The death trigger uses the creature controller when choosing an opponent")
    void deathTriggerUsesCreatureController() {
        Permanent imp = harness.addToBattlefieldAndReturn(player2, new ChimneyImp());
        killImp(imp);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Target opponent chooses a hand card to put on top of their library")
    void putsChosenCardOnTopOfOpponentsLibrary() {
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new ChimneyImp());
        List<Card> hand = List.of(new ElectrostaticBolt(), new ElectrostaticBolt());
        harness.setHand(player2, hand);
        Card oldTop = gd.playerDecks.get(player2.getId()).getFirst();

        killImp(imp);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.playerId()).isEqualTo(player2.getId());
                    assertThat(choice.minCount()).isEqualTo(1);
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
        harness.setHand(player1, List.of(new ElectrostaticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, imp.getId());
    }
}
