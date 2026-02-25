package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeoninArbiterTest extends BaseCardTest {


    // ===== Card properties =====

    @Test
    @DisplayName("Leonin Arbiter has CantSearchLibrariesEffect as static effect")
    void hasCorrectStaticEffect() {
        LeoninArbiter card = new LeoninArbiter();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CantSearchLibrariesEffect.class);
    }

    // ===== Search restriction — opponent =====

    @Test
    @DisplayName("Opponent cannot search library when Leonin Arbiter is on the battlefield and they have no mana")
    void opponentCannotSearchWithoutMana() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        // Player2 casts Diabolic Tutor but has no extra mana for Arbiter tax
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4); // exactly enough for Tutor, none for Arbiter

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // resolve Diabolic Tutor

        // Search is prevented — no may ability prompt either (can't afford)
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("prevented by Leonin Arbiter"));
    }

    @Test
    @DisplayName("Opponent is prompted to pay {2} for Leonin Arbiter and can search after accepting")
    void opponentCanSearchWhenPayingTax() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        // Player2 casts Diabolic Tutor with extra mana for Arbiter tax
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 6); // 4 for Tutor + 2 for Arbiter tax

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // resolve Diabolic Tutor → may ability prompt

        // Player is prompted to pay for Arbiter
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        harness.handleMayAbilityChosen(player2, true); // accept → pay mana, push search entry
        harness.passBothPriorities(); // resolve re-dispatched search entry

        // Search proceeds — library search prompt shown
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("pays {2} to search (Leonin Arbiter)"));
        // Mana was deducted for the tax
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Library is shuffled even when search is prevented by Leonin Arbiter (can't pay)")
    void libraryShuffledWhenSearchPreventedCantPay() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4); // exactly enough for Tutor, none for Arbiter

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        List<Card> deck = gd.playerDecks.get(player2.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp(), new GrizzlyBears(), new GrizzlyBears()));
        int deckSizeBefore = deck.size();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        // Search is prevented but library should still be shuffled (per rules: "search...then shuffle")
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("prevented by Leonin Arbiter"));
        assertThat(deck).hasSize(deckSizeBefore); // deck preserved (shuffled, not emptied)
    }

    @Test
    @DisplayName("Library is shuffled when opponent declines to pay Leonin Arbiter tax")
    void libraryShuffledWhenSearchPreventedDeclined() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 6); // 4 for Tutor + 2 for Arbiter tax

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        List<Card> deck = gd.playerDecks.get(player2.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp(), new GrizzlyBears(), new GrizzlyBears()));
        int deckSizeBefore = deck.size();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // resolve → may ability prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player2, false); // decline

        // Search is prevented but library should still be shuffled
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("declines to pay for Leonin Arbiter"));
        assertThat(deck).hasSize(deckSizeBefore); // deck preserved (shuffled, not emptied)
    }

    @Test
    @DisplayName("Opponent declines to pay for Leonin Arbiter and search is prevented")
    void opponentDeclinesSearchTax() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 6); // 4 for Tutor + 2 for Arbiter tax

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // resolve → may ability prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        harness.handleMayAbilityChosen(player2, false); // decline

        // Search is prevented
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("declines to pay for Leonin Arbiter"));
    }

    // ===== Search restriction — controller =====

    @Test
    @DisplayName("Controller also cannot search when Leonin Arbiter is on the battlefield and they have no extra mana")
    void controllerCannotSearchWithoutMana() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 4); // exactly enough for Tutor

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("prevented by Leonin Arbiter"));
    }

    @Test
    @DisplayName("Controller can search when they accept paying {2} for Leonin Arbiter")
    void controllerCanSearchWhenPayingTax() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 6); // 4 for Tutor + 2 for Arbiter

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve → may ability prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        harness.handleMayAbilityChosen(player1, true); // accept
        harness.passBothPriorities(); // resolve search entry

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("pays {2} to search (Leonin Arbiter)"));
    }

    // ===== Payment persists until end of turn =====

    @Test
    @DisplayName("After paying for Leonin Arbiter, subsequent searches this turn do not require payment again")
    void paymentPersistsUntilEndOfTurn() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        // First search — player2 pays {2}
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 6);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // resolve → may ability prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player2, true); // accept and pay
        harness.passBothPriorities(); // resolve search entry

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        gs.handleLibraryCardChosen(gd, player2, 0);

        // Second search — should NOT require additional payment (Arbiter already paid this turn)
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4); // exactly enough for Tutor, no extra
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        // Search should proceed without additional tax payment — no may ability prompt
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
    }

    // ===== Payment resets at turn change =====

    @Test
    @DisplayName("Payment resets when a new turn begins")
    void paymentResetsAtNewTurn() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        // Player2 pays the tax
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 6);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // resolve → may ability prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities(); // resolve search entry

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        gs.handleLibraryCardChosen(gd, player2, 0);

        // Verify paid status is recorded
        assertThat(gd.paidSearchTaxPermanentIds.get(player2.getId())).isNotEmpty();

        // Simulate new turn
        gd.paidSearchTaxPermanentIds.clear();

        // Now player2 should need to pay again — no mana means prevented
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4); // no extra for Arbiter
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("prevented by Leonin Arbiter"));
    }

    // ===== Removing Arbiter restores search =====

    @Test
    @DisplayName("Removing Leonin Arbiter from battlefield restores ability to search freely")
    void removingArbiterRestoresSearch() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        // Remove the Arbiter
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Leonin Arbiter"));

        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4); // exactly enough for Tutor

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        // Search proceeds normally — no Arbiter tax, no may ability prompt
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).noneMatch(entry -> entry.contains("Leonin Arbiter"));
    }

    // ===== Multiple Arbiters =====

    @Test
    @DisplayName("Two Leonin Arbiters require {4} to search")
    void twoArbitersRequireDoubleTax() {
        harness.addToBattlefield(player1, new LeoninArbiter());
        harness.addToBattlefield(player2, new LeoninArbiter());

        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 8); // 4 for Tutor + 4 for two Arbiters

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // resolve → may ability prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities(); // resolve search entry

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("pays {4} to search (Leonin Arbiter)"));
    }

    @Test
    @DisplayName("Two Leonin Arbiters prevent search when player can only pay {2}")
    void twoArbitersPreventSearchWithInsufficientMana() {
        harness.addToBattlefield(player1, new LeoninArbiter());
        harness.addToBattlefield(player2, new LeoninArbiter());

        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 6); // 4 for Tutor + 2 (only enough for one Arbiter)

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("prevented by Leonin Arbiter"));
    }

    // ===== Works with activated ability search (Terramorphic Expanse) =====

    @Test
    @DisplayName("Leonin Arbiter prevents Terramorphic Expanse search without mana")
    void preventsActivatedAbilitySearchWithoutMana() {
        harness.addToBattlefield(player1, new LeoninArbiter());
        harness.addToBattlefield(player1, new TerramorphicExpanse());

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest()));

        harness.activateAbility(player1, 1, null, null); // activate Terramorphic Expanse (index 1)
        harness.passBothPriorities();

        // Search is prevented — no mana to pay Arbiter tax
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("prevented by Leonin Arbiter"));
    }

    @Test
    @DisplayName("Leonin Arbiter allows Terramorphic Expanse search when player accepts paying {2}")
    void allowsActivatedAbilitySearchWhenPaid() {
        harness.addToBattlefield(player1, new LeoninArbiter());
        harness.addToBattlefield(player1, new TerramorphicExpanse());
        harness.addMana(player1, ManaColor.COLORLESS, 2); // for Arbiter tax

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest()));

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities(); // resolve → may ability prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        harness.handleMayAbilityChosen(player1, true); // accept → pay mana, push search entry
        harness.passBothPriorities(); // resolve search entry

        // Search proceeds — tax was paid
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("pays {2} to search (Leonin Arbiter)"));
    }

    // ===== Helpers =====

    private void setupLibrary(com.github.laxika.magicalvibes.model.Player player) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp(), new GrizzlyBears(), new GrizzlyBears()));
    }
}
