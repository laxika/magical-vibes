package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarwigSquad.class, PricklyBoggart.class})
class EarwigSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Prowl cast after Goblin combat damage exiles three cards from target opponent's library")
    void prowlExilesAfterGoblinDamage() {
        setupProwl(CardSubtype.GOBLIN);

        harness.setHand(player1, List.of(new EarwigSquad()));
        harness.addMana(player1, ManaColor.BLACK, 3); // prowl {2}{B}
        harness.castWithProwl(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Prowl cast after Rogue combat damage also enables the search")
    void prowlExilesAfterRogueDamage() {
        setupProwl(CardSubtype.ROGUE);

        harness.setHand(player1, List.of(new EarwigSquad()));
        harness.addMana(player1, ManaColor.BLACK, 3); // prowl {2}{B}
        harness.castWithProwl(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    @Test
    @DisplayName("Prowl search exiles all available cards when the target library has fewer than three")
    void prowlExilesAllAvailableCardsFromShortLibrary() {
        setupProwl(CardSubtype.GOBLIN);
        harness.setLibrary(player2, List.of(new PricklyBoggart(), new PricklyBoggart()));

        harness.setHand(player1, List.of(new EarwigSquad()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castWithProwl(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Normal cast does not search (intervening-if: prowl not paid)")
    void normalCastDoesNotSearch() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        stockOpponentLibrary();

        harness.setHand(player1, List.of(new EarwigSquad()));
        harness.addMana(player1, ManaColor.BLACK, 5); // normal {3}{B}{B}
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities(); // resolve creature spell

        // Prowl not paid — the intervening-if ETB trigger never goes on the stack.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        harness.assertOnBattlefield(player1, "Earwig Squad");
    }

    @Test
    @DisplayName("Prowl cost is unavailable without combat damage from a Goblin or Rogue")
    void prowlUnavailableWithoutQualifyingDamage() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new EarwigSquad()));
        harness.addMana(player1, ManaColor.BLACK, 3); // enough for prowl {2}{B}, not for {3}{B}{B}

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast targeting yourself")
    void cannotTargetYourself() {
        setupProwl(CardSubtype.GOBLIN);

        harness.setHand(player1, List.of(new EarwigSquad()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Prowl resolves normally when the opponent's library is empty")
    void emptyLibraryDoesNotLeaveSearchPending() {
        setupProwl(CardSubtype.GOBLIN);
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new EarwigSquad()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castWithProwl(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Earwig Squad");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The search cannot be declined or ended before three cards are exiled")
    void mustFindThreeCardsWhenAvailable() {
        setupProwl(CardSubtype.GOBLIN);
        harness.setHand(player1, List.of(new EarwigSquad()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castWithProwl(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Combat damage from Prickly Boggart enables prowl in the second main phase")
    void actualCombatDamageEnablesProwl() {
        addCreatureReady(player1, new PricklyBoggart());
        stockOpponentLibrary();
        declareAttackers(List.of(0));
        resolveCombat();
        harness.assertLife(player2, 19);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new EarwigSquad()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castWithProwl(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Earwig Squad");
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
    }

    private void setupProwl(CardSubtype subtype) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(subtype);
        stockOpponentLibrary();
    }

    private void stockOpponentLibrary() {
        harness.setLibrary(player2, List.of(
                new PricklyBoggart(), new PricklyBoggart(), new PricklyBoggart(), new PricklyBoggart()));
    }
}
