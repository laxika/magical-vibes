package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkRaiders;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NamorScourgeOfTheSeas.class, MerfolkRaiders.class, Forest.class, GrizzlyBears.class})
class NamorScourgeOfTheSeasTest extends BaseCardTest {

    @Test
    @DisplayName("Countered Merfolk you control have flying")
    void counteredOwnMerfolkHaveFlying() {
        addCreatureReady(player1, new NamorScourgeOfTheSeas());
        Permanent counteredMerfolk = addCreatureReady(player1, new MerfolkRaiders());
        Permanent uncounteredMerfolk = addCreatureReady(player1, new MerfolkRaiders());
        Permanent opponentMerfolk = addCreatureReady(player2, new MerfolkRaiders());
        counteredMerfolk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponentMerfolk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, counteredMerfolk, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredMerfolk, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentMerfolk, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Beginning of combat lets a creature you control connive")
    void beginningOfCombatTargetConnives() {
        addCreatureReady(player1, new NamorScourgeOfTheSeas());
        Permanent target = addCreatureReady(player1, new MerfolkRaiders());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Grizzly Bears");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    @DisplayName("Beginning of combat cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new NamorScourgeOfTheSeas());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(opponentCreature.getId());
    }

    @Test
    @DisplayName("Discarding a land while conniving does not add a counter")
    void landDiscardDoesNotAddCounter() {
        addCreatureReady(player1, new NamorScourgeOfTheSeas());
        Permanent target = addCreatureReady(player1, new MerfolkRaiders());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        discardByName("Forest");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("A non-Merfolk can connive but does not gain flying")
    void nonMerfolkCanConnive() {
        Permanent namor = addCreatureReady(player1, new NamorScourgeOfTheSeas());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        discardByName("Grizzly Bears");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(namor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Namor can target itself to connive")
    void namorCanConniveItself() {
        Permanent namor = addCreatureReady(player1, new NamorScourgeOfTheSeas());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, namor.getId());
        harness.passBothPriorities();
        discardByName("Grizzly Bears");

        assertThat(namor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Flying is lost when the last qualifying counter or Namor leaves")
    void flyingGrantTracksCountersAndSource() {
        Permanent namor = addCreatureReady(player1, new NamorScourgeOfTheSeas());
        Permanent merfolk = addCreatureReady(player1, new MerfolkRaiders());
        merfolk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FLYING)).isTrue();

        merfolk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FLYING)).isFalse();

        merfolk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FLYING)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(namor);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Namor does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentTurn() {
        addCreatureReady(player1, new NamorScourgeOfTheSeas());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An illegal connive target prevents drawing and discarding")
    void removedTargetDoesNotConnive() {
        addCreatureReady(player1, new NamorScourgeOfTheSeas());
        Permanent target = addCreatureReady(player1, new MerfolkRaiders());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
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
