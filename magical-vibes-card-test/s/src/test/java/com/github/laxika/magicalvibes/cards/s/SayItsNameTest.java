package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AltanakTheThriceCalled;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SayItsName.class, AltanakTheThriceCalled.class, Forest.class})
class SayItsNameTest extends BaseCardTest {

    @Test
    @DisplayName("Mills three cards before offering the creature or land return")
    void millsThenOffersReturn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest(), new AltanakTheThriceCalled(), new Forest()));

        harness.castFromHand(player1, new SayItsName(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may returns a milled land to hand")
    void acceptsReturnOfMilledLand() {
        Forest land = new Forest();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(land, new AltanakTheThriceCalled(), new AltanakTheThriceCalled()));

        harness.castFromHand(player1, new SayItsName(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The graveyard ability exiles itself and two named cards to return Altanak")
    void exilesNamedCardsAndReturnsAltanak() {
        SayItsName source = new SayItsName();
        SayItsName other = new SayItsName();
        SayItsName third = new SayItsName();
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        harness.setGraveyard(player1, List.of(source, other, third, altanak));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Altanak, the Thrice-Called");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).filteredOn(exiled -> exiled.card().getName().equals("Say Its Name"))
                .hasSize(3);
    }

    @Test
    @DisplayName("The graveyard ability requires two other named cards")
    void requiresTwoOtherNamedCards() {
        SayItsName source = new SayItsName();
        SayItsName other = new SayItsName();
        harness.setGraveyard(player1, List.of(source, other, new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(source, other);
    }

    @Test
    void decliningReturnLeavesMilledCardsInGraveyard() {
        Forest land = new Forest();
        AltanakTheThriceCalled creature = new AltanakTheThriceCalled();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(land, creature, new SayItsName()));

        harness.castFromHand(player1, new SayItsName(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, creature).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returnsCreatureThatWasAlreadyInGraveyard() {
        AltanakTheThriceCalled creature = new AltanakTheThriceCalled();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new SayItsName(), new SayItsName(), new SayItsName()));

        harness.castFromHand(player1, new SayItsName(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature).hasSize(4);
    }

    @Test
    void millsAvailableCardsFromShortLibraryAndReturnsLand() {
        Forest land = new Forest();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(land));

        harness.castFromHand(player1, new SayItsName(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        harness.assertInGraveyard(player1, "Say Its Name");
    }

    @Test
    void noCreatureOrLandCanBeReturned() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new SayItsName(), new SayItsName(), new SayItsName()));

        harness.castFromHand(player1, new SayItsName(), "{1}{G}");
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void findsAltanakInHand() {
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SayItsName(), new SayItsName(), new SayItsName()));
        harness.setHand(player1, List.of(altanak));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.exiledCards).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Altanak, the Thrice-Called").getCard()).isSameAs(altanak);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void findsAltanakInLibrary() {
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        Forest land = new Forest();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SayItsName(), new SayItsName(), new SayItsName()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(land, altanak));

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Altanak, the Thrice-Called").getCard()).isSameAs(altanak);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.exiledCards).hasSize(3);
    }

    @Test
    void canFailToFindAltanakInLibrary() {
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SayItsName(), new SayItsName(), new SayItsName()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), altanak));

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Altanak, the Thrice-Called");
        assertThat(gd.playerDecks.get(player1.getId())).contains(altanak).hasSize(2);
        assertThat(gd.exiledCards).hasSize(3);
    }

    @Test
    void playerChoosesSearchZoneWhenAltanakIsInGraveyardAndLibrary() {
        AltanakTheThriceCalled graveyardAltanak = new AltanakTheThriceCalled();
        AltanakTheThriceCalled libraryAltanak = new AltanakTheThriceCalled();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(
                new SayItsName(), new SayItsName(), new SayItsName(), graveyardAltanak));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryAltanak, new Forest()));

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertNotOnBattlefield(player1, "Altanak, the Thrice-Called");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardAltanak);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setGraveyard(player1, List.of(new SayItsName(), new SayItsName(), new SayItsName()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void cannotActivateWhileStackIsNotEmpty() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SayItsName(), new SayItsName(), new SayItsName()));
        harness.castFromHand(player1, new SayItsName(), "{1}{G}");

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void returnsMilledCreature() {
        AltanakTheThriceCalled creature = new AltanakTheThriceCalled();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest(), creature, new SayItsName()));

        harness.castFromHand(player1, new SayItsName(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature).hasSize(3);
    }

    @Test
    void opponentGraveyardCannotPayNamedCardCost() {
        SayItsName source = new SayItsName();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(source));
        harness.setGraveyard(player2, List.of(new SayItsName(), new SayItsName()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SayItsName(), new SayItsName(), new SayItsName()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.exiledCards).isEmpty();
    }
}
