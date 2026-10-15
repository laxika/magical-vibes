package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PersistentPetitioners;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoAndLiRoyalAdvisors.class, PersistentPetitioners.class, Distress.class,
        GrizzlyBears.class, RestInPeace.class})
class LoAndLiRoyalAdvisorsTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent discarding a card puts a counter on each Advisor you control")
    void opponentDiscardPutsCountersOnAdvisors() {
        Permanent loAndLi = harness.addToBattlefieldAndReturn(player1, new LoAndLiRoyalAdvisors());
        Permanent petitioners = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(loAndLi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(petitioners.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Milling an opponent puts one counter on each Advisor for the mill event")
    void opponentMillPutsOneCounterOnEachAdvisor() {
        Permanent loAndLi = harness.addToBattlefieldAndReturn(player1, new LoAndLiRoyalAdvisors());
        Permanent petitioners = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(loAndLi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(petitioners.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    void selfMillDoesNotTriggerAndCanBePaidWithBlackMana() {
        Permanent loAndLi = harness.addToBattlefieldAndReturn(player1, new LoAndLiRoyalAdvisors());
        List<Card> library = List.of(new LoAndLiRoyalAdvisors(), new LoAndLiRoyalAdvisors(),
                new LoAndLiRoyalAdvisors(), new LoAndLiRoyalAdvisors());
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(loAndLi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void emptyOpponentLibraryDoesNotTrigger() {
        Permanent loAndLi = harness.addToBattlefieldAndReturn(player1, new LoAndLiRoyalAdvisors());
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(loAndLi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void controllerDiscardDoesNotTrigger() {
        Permanent loAndLi = harness.addToBattlefieldAndReturn(player1, new LoAndLiRoyalAdvisors());
        Card discardedCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new Distress(), discardedCard));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(loAndLi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void shortOpponentLibraryStillTriggersOnceAndOnlyControlledAdvisorsGetCounters() {
        Permanent loAndLi = harness.addToBattlefieldAndReturn(player1, new LoAndLiRoyalAdvisors());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingAdvisor = harness.addToBattlefieldAndReturn(player2, new PersistentPetitioners());
        Card lastCard = new LoAndLiRoyalAdvisors();
        harness.setLibrary(player2, List.of(lastCard));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(lastCard);
        assertThat(loAndLi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingAdvisor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void abilityCanBeActivatedRepeatedlyWithoutTapping() {
        Permanent loAndLi = harness.addToBattlefieldAndReturn(player1, new LoAndLiRoyalAdvisors());
        List<Card> library = List.of(new LoAndLiRoyalAdvisors(), new LoAndLiRoyalAdvisors(),
                new LoAndLiRoyalAdvisors(), new LoAndLiRoyalAdvisors(),
                new LoAndLiRoyalAdvisors(), new LoAndLiRoyalAdvisors(),
                new LoAndLiRoyalAdvisors(), new LoAndLiRoyalAdvisors());
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();
        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(loAndLi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(loAndLi.isTapped()).isFalse();
    }

    @Test
    @CardUsed({LoAndLiRoyalAdvisors.class, RestInPeace.class})
    void opponentMillStillTriggersWhenCardsAreExiledInsteadOfEnteringGraveyard() {
        Permanent loAndLi = harness.addToBattlefieldAndReturn(player1, new LoAndLiRoyalAdvisors());
        harness.addToBattlefield(player1, new RestInPeace());
        List<Card> library = List.of(new LoAndLiRoyalAdvisors(), new LoAndLiRoyalAdvisors(),
                new LoAndLiRoyalAdvisors(), new LoAndLiRoyalAdvisors());
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(loAndLi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }
}
