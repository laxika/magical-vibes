package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({IngeniousSmith.class, GrizzlyBears.class, Ornithopter.class, Plains.class})
class IngeniousSmithTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers one artifact from the top four and randomly bottoms the rest")
    void etbOffersArtifactFromTopFour() {
        setupTopCards(List.of(new Ornithopter(), new GrizzlyBears(), new Plains(), new GrizzlyBears()));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice search =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.allCards()).hasSize(4);
        assertThat(search.validCardIds()).hasSize(1);
        Card offeredCard = search.allCards().stream()
                .filter(card -> search.validCardIds().contains(card.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(offeredCard).isInstanceOf(Ornithopter.class);

        harness.handleMultipleCardsChosen(player1, search.validCardIds());

        harness.assertInHand(player1, "Ornithopter");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An artifact entering puts a +1/+1 counter on Ingenious Smith")
    void artifactEntryPutsCounterOnSmith() {
        Permanent smith = harness.addToBattlefieldAndReturn(player1, new IngeniousSmith());

        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(smith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The artifact trigger fires only once each turn")
    void artifactTriggerFiresOnlyOnceEachTurn() {
        Permanent smith = harness.addToBattlefieldAndReturn(player1, new IngeniousSmith());
        harness.setHand(player1, List.of(new Ornithopter(), new Ornithopter()));

        castArtifactAndResolveTrigger();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(smith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The artifact trigger can fire again on a later turn")
    void artifactTriggerFiresAgainOnLaterTurn() {
        Permanent smith = harness.addToBattlefieldAndReturn(player1, new IngeniousSmith());
        harness.setHand(player1, List.of(new Ornithopter()));

        castArtifactAndResolveTrigger();
        advanceTurn();
        advanceTurn();

        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(smith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A nonartifact entering does not trigger the counter ability")
    void nonartifactEntryDoesNotTrigger() {
        Permanent smith = harness.addToBattlefieldAndReturn(player1, new IngeniousSmith());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(smith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new IngeniousSmith()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castArtifactAndResolveTrigger() {
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
