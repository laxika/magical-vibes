package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pantlaza, Sun-Favored")
@CardUsed({PantlazaSunFavored.class, RaptorCompanion.class, GrizzlyBears.class, LlanowarElves.class,
        GiantGrowth.class, Conspiracy.class, Forest.class, SwordsToPlowshares.class})
class PantlazaSunFavoredTest extends BaseCardTest {

    @Test
    @DisplayName("Discovers using the entering Dinosaur's toughness")
    void discoversUsingEnteringDinosaurToughness() {
        addCreatureReady(player1, new PantlazaSunFavored());
        LlanowarElves discovered = new LlanowarElves();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(new RaptorCompanion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
    }

    @Test
    @DisplayName("Does not trigger for a non-Dinosaur creature")
    void doesNotTriggerForNonDinosaur() {
        addCreatureReady(player1, new PantlazaSunFavored());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discoversWhenPantlazaItselfEnters() {
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(new PantlazaSunFavored()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discovered);
    }

    @Test
    void selfEntryStillTriggersWhenConspiracyReplacesDinosaurType() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.ELF);
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));

        harness.enterBattlefieldAndReturn(player1, new PantlazaSunFavored());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discovered);
    }

    @Test
    void doesNotTriggerForOpponentsDinosaur() {
        addCreatureReady(player1, new PantlazaSunFavored());

        harness.enterBattlefieldAndReturn(player2, new RaptorCompanion());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void checksToughnessAtResolutionRatherThanEntry() {
        addCreatureReady(player1, new PantlazaSunFavored());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));
        Permanent dinosaur = harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, dinosaur.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
    }

    @Test
    void usesLastKnownToughnessAfterDinosaurLeaves() {
        addCreatureReady(player1, new PantlazaSunFavored());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));
        Permanent dinosaur = harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new GiantGrowth(), new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, dinosaur.getId());
        harness.castAndResolveInstant(player1, 0, dinosaur.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.assertNotOnBattlefield(player1, "Raptor Companion");
    }

    @Test
    void decliningDoesNotConsumeTheTurnLimit() {
        addCreatureReady(player1, new PantlazaSunFavored());
        LlanowarElves discovered = new LlanowarElves();
        harness.setLibrary(player1, List.of(discovered));
        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(discovered);
        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discovered);
    }

    @Test
    void acceptingDiscoverPreventsFurtherTriggersThatTurn() {
        addCreatureReady(player1, new PantlazaSunFavored());
        LlanowarElves discovered = new LlanowarElves();
        harness.setLibrary(player1, List.of(discovered));
        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void alreadyPendingTriggersCannotDiscoverAgainThatTurn() {
        addCreatureReady(player1, new PantlazaSunFavored());
        LlanowarElves first = new LlanowarElves();
        LlanowarElves second = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canDiscoverAgainOnOpponentsTurn() {
        addCreatureReady(player1, new PantlazaSunFavored());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        LlanowarElves discovered = new LlanowarElves();
        harness.setLibrary(player1, List.of(discovered));
        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    void skipsLandsAndExpensiveCardsThenBottomsOnlySkippedCards() {
        addCreatureReady(player1, new PantlazaSunFavored());
        Forest skippedLand = new Forest();
        GrizzlyBears tooExpensive = new GrizzlyBears();
        LlanowarElves discovered = new LlanowarElves();
        Forest unrevealed = new Forest();
        harness.setLibrary(player1, List.of(skippedLand, tooExpensive, discovered, unrevealed));
        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discovered);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unrevealed);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(unrevealed, skippedLand, tooExpensive);
    }

    @Test
    void castsDiscoveredCardWithoutPayingMana() {
        addCreatureReady(player1, new PantlazaSunFavored());
        LlanowarElves discovered = new LlanowarElves();
        harness.setLibrary(player1, List.of(discovered));
        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void discoversAfterPantlazaLeavesBattlefield() {
        Permanent pantlaza = addCreatureReady(player1, new PantlazaSunFavored());
        LlanowarElves discovered = new LlanowarElves();
        harness.setLibrary(player1, List.of(discovered));
        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, pantlaza.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Pantlaza, Sun-Favored");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discovered);
    }

    @Test
    void discoverWithNoQualifyingCardStillConsumesTheTurnLimit() {
        addCreatureReady(player1, new PantlazaSunFavored());
        Forest land = new Forest();
        GrizzlyBears tooExpensive = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, tooExpensive));
        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, tooExpensive);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        assertThat(gd.stack).isEmpty();
    }
}
