package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarigaazShivanChampion.class})
class DarigaazShivanChampionTest extends BaseCardTest {

    private static final Set<String> SPELLBOOK = Set.of(
            "Shivan Dragon", "Moonveil Regent", "Terror of the Peaks", "Leyline Tyrant",
            "Immersturm Predator", "Manaform Hellkite", "Bone Dragon", "Demanding Dragon",
            "Skarrgan Hellkite", "Thunderbreak Regent", "Black Dragon", "Skyship Stalker",
            "Red Dragon");

    @Test
    @DisplayName("At the controller's end step, Darigaaz conjures a face-down spellbook card with three egg counters")
    void conjuresSpellbookCardIntoExile() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new DarigaazShivanChampion());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.exiledCards)
                .filteredOn(entry -> SPELLBOOK.contains(entry.card().getName()))
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.card().isTokenCard()).isTrue();
                    assertThat(entry.faceDown()).isTrue();
                    assertThat(gd.exiledCardEggCounters).containsEntry(entry.card().getId(), 3);
                });
    }

    @Test
    @DisplayName("The conjured card returns after three of its controller's upkeeps")
    void conjuredCardReturnsAfterThreeUpkeeps() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new DarigaazShivanChampion());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        Card conjuredCard = gd.exiledCards.stream()
                .filter(entry -> SPELLBOOK.contains(entry.card().getName()))
                .map(entry -> entry.card())
                .findFirst()
                .orElseThrow();

        triggerUpkeep(player1);
        assertThat(gd.exiledCardEggCounters).containsEntry(conjuredCard.getId(), 2);
        triggerUpkeep(player1);
        assertThat(gd.exiledCardEggCounters).containsEntry(conjuredCard.getId(), 1);
        triggerUpkeep(player1);

        assertThat(gd.exiledCardEggCounters).doesNotContainKey(conjuredCard.getId());
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(conjuredCard.getId()));
        harness.assertOnBattlefield(player1, conjuredCard.getName());
        assertThat(gd.tokensCreatedThisTurn).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Darigaaz does not conjure during its opponent's end step")
    void doesNotConjureOnOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new DarigaazShivanChampion());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("A conjured card does not lose egg counters during its opponent's upkeep")
    void opponentsUpkeepDoesNotRemoveEggCounter() {
        Card card = conjureCard();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCardEggCounters).containsEntry(card.getId(), 3);
    }

    @Test
    @DisplayName("The gained upkeep ability still triggers with zero egg counters")
    void upkeepTriggersWithZeroEggCounters() {
        Card card = conjureCard();
        gd.exiledCardEggCounters.put(card.getId(), 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, card.getName());
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(card.getId()));
    }

    @Test
    @DisplayName("The conjured card's upkeep ability survives Darigaaz leaving the battlefield")
    void upkeepAbilityIsIndependentOfDarigaaz() {
        Card card = conjureCard();
        gd.playerBattlefields.get(player1.getId()).clear();

        triggerUpkeep(player1);

        assertThat(gd.exiledCardEggCounters).containsEntry(card.getId(), 2);
    }

    private Card conjureCard() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new DarigaazShivanChampion());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        return gd.exiledCards.getFirst().card();
    }

    private void triggerUpkeep(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();
    }
}
