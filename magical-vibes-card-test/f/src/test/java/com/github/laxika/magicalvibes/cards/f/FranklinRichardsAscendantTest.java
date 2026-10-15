package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({FranklinRichardsAscendant.class, GrizzlyBears.class, Shock.class})
class FranklinRichardsAscendantTest extends BaseCardTest {

    @Test
    @DisplayName("Discovers 6 at the beginning of combat after casting a noncreature spell")
    void discoversAfterCastingNoncreatureSpell() {
        harness.addToBattlefield(player1, new FranklinRichardsAscendant());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));

        castNoncreatureSpell();
        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == discovered);
    }

    @Test
    @DisplayName("May put the discovered card into hand")
    void mayPutDiscoveredCardIntoHand() {
        harness.addToBattlefield(player1, new FranklinRichardsAscendant());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));

        castNoncreatureSpell();
        advanceToBeginningOfCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == discovered);
    }

    @Test
    @DisplayName("Does not discover after casting only a creature spell")
    void doesNotDiscoverAfterCastingCreatureSpell() {
        Permanent franklin = harness.addToBattlefieldAndReturn(player1, new FranklinRichardsAscendant());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactlyInAnyOrder(franklin, findPermanent(player1, "Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not trigger when no spell was cast before combat")
    void doesNotTriggerWithoutCastingSpell() {
        harness.addToBattlefield(player1, new FranklinRichardsAscendant());
        GrizzlyBears top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Does not trigger at an opponent's beginning of combat")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new FranklinRichardsAscendant());
        GrizzlyBears top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Discover accepts a card with mana value exactly six and leaves later cards alone")
    void discoversCardAtManaValueBoundary() {
        harness.addToBattlefield(player1, new FranklinRichardsAscendant());
        FranklinRichardsAscendant discovered = new FranklinRichardsAscendant();
        Shock remaining = new Shock();
        harness.setLibrary(player1, List.of(discovered, remaining));

        castNoncreatureSpell();
        advanceToBeginningOfCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("The discovered card is exiled while the cast-or-hand choice is pending")
    void exilesDiscoveredCardBeforeChoice() {
        harness.addToBattlefield(player1, new FranklinRichardsAscendant());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));

        castNoncreatureSpell();
        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() == discovered);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card() == discovered);
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    private void castNoncreatureSpell() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }

    private void advanceToBeginningOfCombat() {
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
