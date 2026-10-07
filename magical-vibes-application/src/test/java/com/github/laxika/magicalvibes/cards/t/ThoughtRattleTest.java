package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RatColony;
import com.github.laxika.magicalvibes.cards.v.ValMaroonedSurveyor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtRattle.class, Forest.class, GrizzlyBears.class, RatColony.class})
class ThoughtRattleTest extends BaseCardTest {

    @Test
    void exilesChosenNonlandCardFromTargetOpponentsHand() {
        harness.setHand(player1, List.of(new ThoughtRattle()));
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        addThoughtRattleMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    void thresholdSeeksRatReducesOnlyThatCardAndGainsLifeForRatsInHand() {
        harness.setHand(player1, List.of(new ThoughtRattle(), new RatColony()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new RatColony()));
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 10);
        addThoughtRattleMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Rat Colony", "Rat Colony");

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void thresholdDoesNotSeekBeforeSevenCards() {
        harness.setHand(player1, List.of(new ThoughtRattle()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        RatColony libraryRat = new RatColony();
        harness.setLibrary(player1, List.of(libraryRat));
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addThoughtRattleMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryRat);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Rat Colony"));
    }

    @Test
    void canTargetOnlyAnOpponent() {
        harness.setHand(player1, List.of(new ThoughtRattle()));
        addThoughtRattleMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void revealsOnlyNonlandCardsToBothPlayers() throws Exception {
        Forest land = new Forest();
        RatColony rat = new RatColony();
        harness.setHand(player1, List.of(new ThoughtRattle()));
        harness.setHand(player2, List.of(land, rat));
        addThoughtRattleMana();
        List<GameEventEnvelope> events = new ArrayList<>();

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            harness.castAndResolveSorcery(player1, 0, player2.getId());
            harness.handleCardChosen(player1, 1);
        }

        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal reveal
                        && reveal.subjectPlayerId().equals(player2.getId())
                        && reveal.zone() == GameEventFact.RevealZone.HAND)
                .isNotEmpty()
                .allSatisfy(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::cardId)
                            .containsExactly(rat.getId());
                    assertThat(event.audience().playerIds())
                            .containsExactlyInAnyOrder(player1.getId(), player2.getId());
                });
    }

    @Test
    void gainsLifeWithoutThresholdWhenOpponentHasOnlyLands() {
        Forest land = new Forest();
        harness.setHand(player1, List.of(new ThoughtRattle(), new RatColony(), new RatColony()));
        harness.setHand(player2, List.of(land));
        harness.setLife(player1, 10);
        addThoughtRattleMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 12);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void thresholdWithNoRatInLibraryStillGainsLifeForExistingRats() {
        Forest libraryLand = new Forest();
        harness.setHand(player1, List.of(new ThoughtRattle(), new RatColony()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(libraryLand));
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 10);
        addThoughtRattleMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 11);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryLand);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({ValMaroonedSurveyor.class})
    void successfulThresholdSeekTriggersVal() {
        harness.addToBattlefield(player1, new ValMaroonedSurveyor());
        harness.setHand(player1, List.of(new ThoughtRattle()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new RatColony()));
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 10);
        addThoughtRattleMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 18);
    }

    private void addThoughtRattleMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
