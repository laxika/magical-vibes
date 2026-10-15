package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
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

@CardUsed({IngeniousSmith.class, GrizzlyBears.class, Ornithopter.class, Plains.class, Panharmonicon.class})
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

    @Test
    @DisplayName("The artifact selection may be declined and all four cards go below the untouched library")
    void mayDeclineArtifactSelection() {
        Card artifact = new Ornithopter();
        List<Card> lookedAt = List.of(artifact, new GrizzlyBears(), new Plains(), new Plains());
        Card untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), untouched));

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5).startsWith(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no artifact in the top four, all four go below the untouched library")
    void noArtifactInTopFour() {
        List<Card> lookedAt = List.of(new Plains(), new GrizzlyBears(), new Plains(), new GrizzlyBears());
        Card untouchedArtifact = new Ornithopter();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), untouchedArtifact));

        castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5).startsWith(untouchedArtifact);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short library permits choosing exactly one of multiple artifacts")
    void shortLibraryOffersMultipleArtifacts() {
        Card first = new Ornithopter();
        Card second = new Ornithopter();
        harness.setLibrary(player1, List.of(first, second));

        castAndResolveEtb();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library finishes the ETB ability without a choice")
    void emptyLibraryFinishesEtb() {
        harness.setLibrary(player1, List.of());

        castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's artifact does not consume Smith's trigger for that turn")
    void opposingArtifactDoesNotConsumeTrigger() {
        Permanent smith = harness.addToBattlefieldAndReturn(player1, new IngeniousSmith());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(smith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Ornithopter()));
        castArtifactAndResolveTrigger();

        assertThat(smith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Smith independently gets one counter per turn")
    void multipleSmithsHaveIndependentTriggerLimits() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IngeniousSmith());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IngeniousSmith());
        harness.setHand(player1, List.of(new Ornithopter(), new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Panharmonicon cannot make the once-per-turn counter ability trigger twice")
    void panharmoniconDoesNotDuplicateCounterTrigger() {
        Permanent smith = harness.addToBattlefieldAndReturn(player1, new IngeniousSmith());
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(smith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
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
