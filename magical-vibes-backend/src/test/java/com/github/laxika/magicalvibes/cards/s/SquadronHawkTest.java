package com.github.laxika.magicalvibes.cards.s;

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

class SquadronHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Squadron Hawk has ETB may-search effect")
    void hasCorrectEffect() {
        SquadronHawk card = new SquadronHawk();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(MayEffect.class);

        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(SearchLibraryForCardsByNameToHandEffect.class);

        SearchLibraryForCardsByNameToHandEffect searchEffect =
                (SearchLibraryForCardsByNameToHandEffect) mayEffect.wrapped();
        assertThat(searchEffect.cardName()).isEqualTo("Squadron Hawk");
        assertThat(searchEffect.maxCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving Squadron Hawk creates may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Squadron Hawk"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining may ability does not search library")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibraryWithHawks(3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).noneMatch(entry -> entry.contains("searches their library"));
    }

    @Test
    @DisplayName("Accepting may ability initiates library search showing only Squadron Hawks")
    void acceptingMayInitiatesSearch() {
        setupAndCast();
        setupLibraryWithHawks(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.getName().equals("Squadron Hawk"));
        assertThat(gd.interaction.librarySearch().cards()).hasSize(2);
    }

    @Test
    @DisplayName("Choosing a Squadron Hawk from search puts it into hand")
    void choosingHawkPutsItIntoHand() {
        setupAndCast();
        setupLibraryWithHawks(3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Squadron Hawk"));
    }

    @Test
    @DisplayName("Can search for multiple hawks sequentially")
    void canSearchForMultipleHawks() {
        setupAndCast();
        setupLibraryWithHawks(3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Pick first hawk
        gs.handleLibraryCardChosen(gd, player1, 0);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Pick second hawk
        gs.handleLibraryCardChosen(gd, player1, 0);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Pick third hawk
        gs.handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        long hawksInHand = gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Squadron Hawk"))
                .count();
        assertThat(hawksInHand).isEqualTo(3);
    }

    @Test
    @DisplayName("Can fail to find early by choosing no card")
    void canFailToFindEarly() {
        setupAndCast();
        setupLibraryWithHawks(3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Pick first hawk
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Fail to find (pass on second pick)
        gs.handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
    }

    @Test
    @DisplayName("No hawks in library results in shuffled library and no search prompt")
    void noHawksInLibraryShuffles() {
        setupAndCast();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no cards named Squadron Hawk"));
    }

    @Test
    @DisplayName("Cards found are revealed")
    void cardsAreRevealed() {
        setupAndCast();
        setupLibraryWithHawks(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.librarySearch().reveals()).isTrue();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new SquadronHawk()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
    }

    private void setupLibraryWithHawks(int hawkCount) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        for (int i = 0; i < hawkCount; i++) {
            deck.add(new SquadronHawk());
        }
        deck.add(new GrizzlyBears());
        deck.add(new GrizzlyBears());
    }
}
