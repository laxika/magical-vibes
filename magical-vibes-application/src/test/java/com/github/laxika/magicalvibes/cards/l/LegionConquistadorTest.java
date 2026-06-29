package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsByNameToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LegionConquistadorTest extends BaseCardTest {

    @Test
    @DisplayName("Legion Conquistador has ETB may-search effect")
    void hasCorrectEffect() {
        LegionConquistador card = new LegionConquistador();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(MayEffect.class);

        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(SearchLibraryForCardsByNameToHandEffect.class);

        SearchLibraryForCardsByNameToHandEffect searchEffect =
                (SearchLibraryForCardsByNameToHandEffect) mayEffect.wrapped();
        assertThat(searchEffect.cardName()).isEqualTo("Legion Conquistador");
        assertThat(searchEffect.maxCount()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("Resolving Legion Conquistador creates may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Legion Conquistador"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining may ability does not search library")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibraryWithConquistadors(3);

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).noneMatch(entry -> entry.contains("searches their library"));
    }

    @Test
    @DisplayName("Accepting may ability initiates library search showing only Legion Conquistadors")
    void acceptingMayInitiatesSearch() {
        setupAndCast();
        setupLibraryWithConquistadors(2);

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.getName().equals("Legion Conquistador"));
        assertThat(gd.interaction.librarySearch().cards()).hasSize(2);
    }

    @Test
    @DisplayName("Choosing a Legion Conquistador from search puts it into hand")
    void choosingConquistadorPutsItIntoHand() {
        setupAndCast();
        setupLibraryWithConquistadors(3);

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Legion Conquistador"));
    }

    @Test
    @DisplayName("Can search for all copies in library (any number)")
    void canSearchForAllCopies() {
        setupAndCast();
        setupLibraryWithConquistadors(3);

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Pick all three copies
        gs.handleLibraryCardChosen(gd, player1, 0);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        gs.handleLibraryCardChosen(gd, player1, 0);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        gs.handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        long conquistadorsInHand = gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Legion Conquistador"))
                .count();
        assertThat(conquistadorsInHand).isEqualTo(3);
    }

    @Test
    @DisplayName("Can fail to find early by choosing no card")
    void canFailToFindEarly() {
        setupAndCast();
        setupLibraryWithConquistadors(3);

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Pick first copy
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Fail to find (pass on second pick)
        gs.handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
    }

    @Test
    @DisplayName("No copies in library results in no search prompt")
    void noCopiesInLibrary() {
        setupAndCast();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no cards named Legion Conquistador"));
    }

    @Test
    @DisplayName("Cards found are revealed")
    void cardsAreRevealed() {
        setupAndCast();
        setupLibraryWithConquistadors(1);

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.librarySearch().reveals()).isTrue();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new LegionConquistador()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }

    private void setupLibraryWithConquistadors(int count) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        for (int i = 0; i < count; i++) {
            deck.add(new LegionConquistador());
        }
        deck.add(new GrizzlyBears());
        deck.add(new GrizzlyBears());
    }
}
