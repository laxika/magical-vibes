package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EarwigSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Prowl cast after Goblin combat damage exiles three cards from target opponent's library")
    void prowlExilesAfterGoblinDamage() {
        setupProwl(CardSubtype.GOBLIN);

        harness.setHand(player1, List.of(new EarwigSquad()));
        harness.addMana(player1, ManaColor.BLACK, 3); // prowl {2}{B}
        harness.castWithProwl(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger -> library search

        gs.handleLibraryCardChosen(gd, player1, 0);
        gs.handleLibraryCardChosen(gd, player1, 0);
        gs.handleLibraryCardChosen(gd, player1, 0);

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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Earwig Squad"));
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

    private void setupProwl(CardSubtype subtype) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(subtype);
        stockOpponentLibrary();
    }

    private void stockOpponentLibrary() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card swamp = new Swamp();
        Card bears2 = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(bears, shock, swamp, bears2));
    }
}
