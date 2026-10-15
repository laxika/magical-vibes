package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PanickedAltisaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChimilTheInnerSun.class, Cancel.class, Forest.class, GrizzlyBears.class, PanickedAltisaur.class})
class ChimilTheInnerSunTest extends BaseCardTest {

    @Test
    @DisplayName("Discovers 5 at the beginning of its controller's end step")
    void discoversFiveAtControllerEndStep() {
        harness.addToBattlefield(player1, new ChimilTheInnerSun());
        GrizzlyBears discovered = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, discovered));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Can cast the discovered card without paying its mana cost")
    void castsDiscoveredCardForFree() {
        harness.addToBattlefield(player1, new ChimilTheInnerSun());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == discovered
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN))
                .isZero();
    }

    @Test
    @DisplayName("Does not discover during an opponent's end step")
    void doesNotDiscoverDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new ChimilTheInnerSun());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spells controlled by its controller cannot be countered")
    void controllerSpellsCannotBeCountered() {
        harness.addToBattlefield(player1, new ChimilTheInnerSun());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Discover exiles the skipped cards and the discovered card before the choice")
    void discoverCardsAreInExileWhileChoosing() {
        harness.addToBattlefield(player1, new ChimilTheInnerSun());
        Forest land = new Forest();
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, discovered));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .containsExactlyInAnyOrder(land, discovered);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Discover skips a six-mana nonland card and bottoms it after untouched cards")
    void skipsCardsAboveFive() {
        harness.addToBattlefield(player1, new ChimilTheInnerSun());
        ChimilTheInnerSun expensive = new ChimilTheInnerSun();
        GrizzlyBears discovered = new GrizzlyBears();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(expensive, discovered, untouched));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, expensive);
    }

    @Test
    @DisplayName("Discover returns all cards when the library contains no qualifying card")
    void noQualifyingCardReturnsLibrary() {
        harness.addToBattlefield(player1, new ChimilTheInnerSun());
        Forest land = new Forest();
        ChimilTheInnerSun expensive = new ChimilTheInnerSun();
        harness.setLibrary(player1, List.of(land, expensive));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An uncastable discovered counterspell goes to hand")
    void counterspellWithoutLegalSpellTargetGoesToHand() {
        harness.addToBattlefield(player1, new ChimilTheInnerSun());
        Cancel discovered = new Cancel();
        harness.setLibrary(player1, List.of(discovered));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chimil does not protect spells controlled by its opponent")
    void opponentsSpellsCanBeCountered() {
        harness.addToBattlefield(player2, new ChimilTheInnerSun());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Chimil can be countered before its static ability becomes active")
    void doesNotProtectItselfOnTheStack() {
        harness.setHand(player1, List.of(new ChimilTheInnerSun()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        var chimil = gd.playerHands.get(player1.getId()).getFirst();

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, chimil.getId());

        harness.assertInGraveyard(player1, "Chimil, the Inner Sun");
        harness.assertNotOnBattlefield(player1, "Chimil, the Inner Sun");
    }

    @Test
    @DisplayName("Discover finishes normally with an empty library")
    void emptyLibraryDoesNotRequireChoice() {
        harness.addToBattlefield(player1, new ChimilTheInnerSun());
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discover can cast a card with mana value exactly five during the end step")
    void castsCardAtFiveManaBoundary() {
        harness.addToBattlefield(player1, new ChimilTheInnerSun());
        PanickedAltisaur discovered = new PanickedAltisaur();
        harness.setLibrary(player1, List.of(discovered));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Panicked Altisaur");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
