package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DampenThought.class, ReachThroughMists.class})
class DampenThoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Mills four cards from target player's library")
    void millsFourCards() {
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player2, gd.playerDecks.get(player2.getId()).subList(gd.playerDecks.get(player2.getId()).size() - 10, gd.playerDecks.get(player2.getId()).size()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Mills only the remaining cards when the library has fewer than four")
    void millsOnlyRemaining() {
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player2, gd.playerDecks.get(player2.getId()).subList(gd.playerDecks.get(player2.getId()).size() - 2, gd.playerDecks.get(player2.getId()).size()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        ReachThroughMists arcaneSpell = new ReachThroughMists();
        DampenThought dampenThought = new DampenThought();
        harness.setHand(player1, List.of(arcaneSpell, dampenThought));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player2, gd.playerDecks.get(player2.getId()).subList(gd.playerDecks.get(player2.getId()).size() - 10, gd.playerDecks.get(player2.getId()).size()));
        int player1DeckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).contains(dampenThought);
    }

    @Test
    @DisplayName("Can target its controller and mills the top four cards")
    void millsController() {
        DampenThought spell = new DampenThought();
        List<DampenThought> library = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> new DampenThought()).toList();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(4));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsAll(library.subList(0, 4)).contains(spell).doesNotContain(library.get(4));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling an empty library does not cause a player to lose")
    void millsEmptyLibrary() {
        harness.setHand(player1, List.of(new DampenThought()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("A spliced Dampen Thought can subsequently be cast normally")
    void castsAfterSplicing() {
        DampenThought dampenThought = new DampenThought();
        harness.setHand(player1, List.of(new ReachThroughMists(), dampenThought));
        harness.setLibrary(player2, gd.playerDecks.get(player2.getId()).subList(gd.playerDecks.get(player2.getId()).size() - 10, gd.playerDecks.get(player2.getId()).size()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(dampenThought);

        harness.castInstant(player1, gd.playerHands.get(player1.getId()).indexOf(dampenThought), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dampenThought);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dampenThought);
    }
}
