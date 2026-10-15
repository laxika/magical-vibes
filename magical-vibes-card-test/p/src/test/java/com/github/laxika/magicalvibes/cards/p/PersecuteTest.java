package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LibraryOfLeng;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AirElemental.class, DryadArbor.class, Forest.class, GrizzlyBears.class, LibraryOfLeng.class,
        Persecute.class, Watchwolf.class})
class PersecuteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Persecute awaits the caster's color choice")
    void resolvingAwaitsColorChoice() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new AirElemental()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Resolving Persecute awaits the caster's color choice")
    void resolvingAwaitsColorChoiceUpstreamReview() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new AirElemental()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Target player discards all cards of the chosen color, keeping the rest")
    void discardsAllCardsOfChosenColor() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new AirElemental()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .matches(c -> c.getName().equals("Air Elemental"));
    }

    @Test
    @DisplayName("Target player discards all cards of the chosen color, keeping the rest")
    void discardsAllCardsOfChosenColorUpstreamReview() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new AirElemental()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .matches(c -> c.getName().equals("Air Elemental"));
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("reveals their hand"));
    }

    @Test
    @DisplayName("Choosing a color the target has none of discards nothing")
    void chosenColorAbsentDiscardsNothing() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new AirElemental()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Choosing a color the target has none of discards nothing")
    void chosenColorAbsentDiscardsNothingUpstreamReview() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new AirElemental()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An ordinary colorless land is never discarded")
    void colorlessCardsAreNeverDiscarded() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "GREEN");

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .matches(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("A colored land is discarded when it is of the chosen color")
    void discardsColoredLandOfChosenColor() {
        harness.setHand(player2, List.of(new DryadArbor()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "GREEN");

        harness.assertInGraveyard(player2, "Dryad Arbor");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A multicolored card is discarded when it contains the chosen color")
    void discardsMulticoloredCardContainingChosenColor() {
        harness.setHand(player2, List.of(new Watchwolf(), new AirElemental()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "GREEN");

        harness.assertInGraveyard(player2, "Watchwolf");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .matches(c -> c.getName().equals("Air Elemental"));
    }

    @Test
    @DisplayName("Persecute respects a discard replacement that puts the card on top of the library")
    void respectsDiscardToTopOfLibraryReplacement() {
        harness.addToBattlefield(player2, new LibraryOfLeng());
        harness.setLibrary(player2, List.of(new AirElemental()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()))
                .first()
                .matches(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @CardUsed(DryadArbor.class)
    @DisplayName("A colored land is discarded when it is of the chosen color")
    void discardsColoredLandOfChosenColorUpstreamReview() {
        harness.setHand(player2, List.of(new DryadArbor(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "GREEN");

        harness.assertInGraveyard(player2, "Dryad Arbor");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Target player may be the caster")
    void canTargetCaster() {
        harness.setHand(player1, List.of(new Persecute(), new GrizzlyBears(), new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleListChoice(player1, "GREEN");

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .matches(c -> c.getName().equals("Air Elemental"));
    }

    @Test
    @DisplayName("Resolving against an empty hand still resolves the color choice with no discards")
    void emptyHandDiscardsNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The target may be the spell's controller")
    void canTargetController() {
        harness.setHand(player1, List.of(new Persecute(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleListChoice(player1, "GREEN");

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolving Persecute emits a public reveal of the target hand")
    void emitsPublicHandRevealEvent() throws Exception {
        List<GameEventEnvelope> emittedEvents = new ArrayList<>();
        harness.setHand(player2, List.of(new GrizzlyBears(), new AirElemental()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch ->
                batch.events().forEach(emittedEvents::add))) {
            harness.castAndResolveSorcery(player1, 0, player2.getId());
            harness.handleListChoice(player1, "GREEN");
        }

        assertThat(emittedEvents)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal)
                .anySatisfy(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.subjectPlayerId()).isEqualTo(player2.getId());
                    assertThat(reveal.zone()).isEqualTo(GameEventFact.RevealZone.HAND);
                    assertThat(reveal.cards())
                            .extracting(GameEventFact.CardSnapshot::name)
                            .containsExactly("Grizzly Bears", "Air Elemental");
                    assertThat(event.audience().playerIds())
                            .containsExactlyInAnyOrder(player1.getId(), player2.getId());
                });
    }

    @Test
    @DisplayName("The target may decline Library of Leng's replacement when Persecute makes them discard")
    void mayDeclineDiscardToLibraryReplacement() {
        harness.addToBattlefield(player2, new LibraryOfLeng());
        harness.setLibrary(player2, List.of(new AirElemental()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()))
                .singleElement()
                .matches(c -> c.getName().equals("Air Elemental"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The target's hand stays hidden until the caster has chosen a color")
    void revealsHandOnlyAfterColorChoice() throws Exception {
        List<GameEventEnvelope> emittedEvents = new ArrayList<>();
        harness.setHand(player2, List.of(new GrizzlyBears(), new AirElemental()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch ->
                batch.events().forEach(emittedEvents::add))) {
            harness.castAndResolveSorcery(player1, 0, player2.getId());

            assertThat(emittedEvents).noneMatch(event -> event.fact() instanceof GameEventFact.PrivateReveal);
            assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

            harness.handleListChoice(player1, "BLUE");
        }

        assertThat(emittedEvents).anyMatch(event -> event.fact() instanceof GameEventFact.PrivateReveal);
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
