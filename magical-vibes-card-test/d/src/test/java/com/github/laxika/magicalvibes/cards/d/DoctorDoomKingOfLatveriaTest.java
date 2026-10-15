package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MODOK;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoctorDoomKingOfLatveria.class, DangerousWager.class, Forest.class,
        GrizzlyBears.class, MODOK.class})
class DoctorDoomKingOfLatveriaTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 2 life once when a discard event includes a land")
    void losesLifeForLandInDiscardEvent() {
        harness.addToBattlefield(player1, new DoctorDoomKingOfLatveria());
        harness.setHand(player1, new ArrayList<>(List.of(
                new DangerousWager(), new Forest(), new GrizzlyBears())));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when the discard event contains no land")
    void doesNotTriggerForNonlandDiscardEvent() {
        harness.addToBattlefield(player1, new DoctorDoomKingOfLatveria());
        harness.setHand(player1, new ArrayList<>(List.of(new DangerousWager(), new GrizzlyBears())));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Targets a Villain you control, grants menace, and connives")
    void targetsVillainGrantsMenaceAndConnives() {
        harness.addToBattlefield(player1, new DoctorDoomKingOfLatveria());
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new MODOK());
        Permanent nonVillain = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentVillain = harness.addToBattlefieldAndReturn(player2, new MODOK());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(villain.getId())
                .doesNotContain(nonVillain.getId(), opponentVillain.getId());

        harness.handlePermanentChosen(player1, villain.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, villain, Keyword.MENACE)).isTrue();
        assertThat(villain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, villain, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Discarding multiple lands together causes only one life-loss trigger")
    void multipleLandsInOneDiscardEventTriggerOnce() {
        harness.addToBattlefield(player1, new DoctorDoomKingOfLatveria());
        harness.setHand(player1, List.of(new DangerousWager(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent discarding lands does not trigger Doctor Doom")
    void opponentsLandDiscardDoesNotTrigger() {
        harness.addToBattlefield(player1, new DoctorDoomKingOfLatveria());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DangerousWager(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Conniving with a land grants menace without a counter and triggers life loss")
    void landConniveTriggersLifeLossWithoutCounter() {
        Permanent doom = harness.addToBattlefieldAndReturn(player1, new DoctorDoomKingOfLatveria());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, doom.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, doom, Keyword.MENACE)).isTrue();
        assertThat(doom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The combat ability does not trigger during an opponent's turn")
    void doesNotTriggerAtOpponentsCombat() {
        Permanent doom = harness.addToBattlefieldAndReturn(player1, new DoctorDoomKingOfLatveria());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, doom, Keyword.MENACE)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
