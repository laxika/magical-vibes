package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import com.github.laxika.magicalvibes.cards.p.PsychicPurge;
import com.github.laxika.magicalvibes.cards.r.RielleTheEverwise;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HintOfInsanity.class, AvenFlock.class, DuskImp.class, Forest.class,
        RielleTheEverwise.class, TamiyoCollectorOfTales.class, ObstinateBaloth.class, PsychicPurge.class})
class HintOfInsanityTest extends BaseCardTest {

    @Test
    @DisplayName("Discards all duplicate nonland cards and leaves unique cards and lands")
    void discardsDuplicateNonlandCards() {
        harness.setHand(player2, List.of(new DuskImp(), new DuskImp(), new Forest(), new AvenFlock()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Aven Flock");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Dusk Imp", "Dusk Imp");
    }

    @Test
    @DisplayName("A hand without duplicate nonland names is unchanged")
    void noDuplicateNonlandNamesDoesNothing() {
        harness.setHand(player2, List.of(new DuskImp(), new Forest(), new AvenFlock()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Dusk Imp", "Forest", "Aven Flock");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Duplicate lands remain in hand while duplicate nonlands are discarded")
    void duplicateLandsRemainInHand() {
        harness.setHand(player2, List.of(
                new DuskImp(), new DuskImp(), new Forest(), new Forest(), new AvenFlock()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Forest", "Aven Flock");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Dusk Imp", "Dusk Imp");
    }

    @Test
    @DisplayName("Counts every card discarded in one discard event")
    void countsEveryCardDiscardedInOneEvent() {
        harness.addToBattlefield(player2, new RielleTheEverwise());
        harness.setHand(player2, List.of(new DuskImp(), new DuskImp(), new Forest()));
        harness.setLibrary(player2, List.of(new AvenFlock(), new AvenFlock()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Aven Flock", "Aven Flock");
    }

    @Test
    @DisplayName("An opponent's discard-prevention effect stops the discard")
    void opponentDiscardPreventionStopsTheDiscard() {
        harness.addToBattlefieldAndReturn(player2, new TamiyoCollectorOfTales()).setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player2, List.of(new DuskImp(), new DuskImp(), new Forest()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Dusk Imp", "Dusk Imp", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A duplicate creature with a discard replacement enters the battlefield")
    void discardReplacementPutsMatchingCreatureOntoBattlefield() {
        harness.setHand(player2, List.of(new ObstinateBaloth(), new ObstinateBaloth(), new Forest()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Obstinate Baloth", "Obstinate Baloth");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discarding your own duplicate cards does not trigger opponent-discard abilities")
    void selfTargetDoesNotCountAsOpponentCausedDiscard() {
        harness.setHand(player1, List.of(
                new HintOfInsanity(), new PsychicPurge(), new PsychicPurge(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Discards every copy in multiple duplicate groups, including three copies")
    void discardsAllCopiesOfEveryDuplicateName() {
        harness.setHand(player2, List.of(new DuskImp(), new AvenFlock(), new DuskImp(),
                new Forest(), new AvenFlock(), new DuskImp()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Dusk Imp", "Dusk Imp", "Dusk Imp", "Aven Flock", "Aven Flock");
    }

    @Test
    @DisplayName("An empty target hand resolves without discarding anything")
    void emptyHandResolvesNormally() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Hint of Insanity");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reveals the entire original hand to both players before discarding duplicates")
    void revealsEntireHandToBothPlayers() throws Exception {
        List<GameEventEnvelope> emittedEvents = new ArrayList<>();
        harness.setHand(player2, List.of(new DuskImp(), new DuskImp(), new Forest(), new AvenFlock()));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch ->
                batch.events().forEach(emittedEvents::add))) {
            harness.castAndResolveSorcery(player1, 0, player2.getId());
        }

        assertThat(emittedEvents)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal)
                .anySatisfy(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.subjectPlayerId()).isEqualTo(player2.getId());
                    assertThat(reveal.zone()).isEqualTo(GameEventFact.RevealZone.HAND);
                    assertThat(reveal.cards())
                            .extracting(GameEventFact.CardSnapshot::name)
                            .containsExactly("Dusk Imp", "Dusk Imp", "Forest", "Aven Flock");
                    assertThat(event.audience().playerIds())
                            .containsExactlyInAnyOrder(player1.getId(), player2.getId());
                });
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Aven Flock");
    }
}
