package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoninArbiter.class, DiabolicTutor.class, Forest.class, GrizzlyBears.class,
        Plains.class, Swamp.class, TerramorphicExpanse.class})
class LeoninArbiterTest extends BaseCardTest {

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

        // Search is prevented — no mana to pay
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("prevented by Leonin Arbiter"));
    }

    @Test
    @DisplayName("Opponent can search after paying search tax as special action during priority")
    void opponentCanSearchWhenPayingTaxDuringPriority() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        // Player2 casts Diabolic Tutor with extra mana for Arbiter tax
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 6); // 4 for Tutor + 2 for Arbiter tax

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);

        // While Diabolic Tutor is on the stack, pay the search tax as a special action
        harness.paySearchTax(player2);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("pays {2} for Leonin Arbiter search tax"));
        // Mana deducted for the tax (had 2 remaining after casting Tutor)
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();

        harness.passBothPriorities(); // resolve Diabolic Tutor

        // Search proceeds — tax was paid during priority
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("Search is prevented when opponent does not pay search tax before resolution")
    void searchPreventedWhenNotPaid() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 6); // 4 for Tutor + 2 available but not paid

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);
        // Do NOT pay search tax — just pass priority
        harness.passBothPriorities(); // resolve Diabolic Tutor

        // Search is prevented
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("prevented by Leonin Arbiter"));
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

        harness.setLibrary(player2, List.of(new Plains(), new Swamp(), new GrizzlyBears(), new GrizzlyBears()));
        List<Card> deck = gd.playerDecks.get(player2.getId());
        int deckSizeBefore = deck.size();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        // Search is prevented but library should still be shuffled (per rules: "search...then shuffle")
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("prevented by Leonin Arbiter"));
        assertThat(deck).hasSize(deckSizeBefore); // deck preserved (shuffled, not emptied)
    }

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

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("prevented by Leonin Arbiter"));
    }

    @Test
    @DisplayName("Controller can search when they pay search tax during priority")
    void controllerCanSearchWhenPayingTax() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 6); // 4 for Tutor + 2 for Arbiter

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player1);

        harness.castSorcery(player1, 0, 0);

        // Pay search tax while Tutor is on the stack
        harness.paySearchTax(player1);

        harness.passBothPriorities(); // resolve Diabolic Tutor

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("pays {2} for Leonin Arbiter search tax"));
    }

    @Test
    @DisplayName("After paying for Leonin Arbiter, subsequent searches this turn do not require payment again")
    void paymentPersistsUntilEndOfTurn() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        // First search — player2 pays {2} during priority
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 6);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);
        harness.paySearchTax(player2);
        harness.passBothPriorities(); // resolve Diabolic Tutor

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player2, 0);

        // Second search — should NOT require additional payment (Arbiter already paid this turn)
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4); // exactly enough for Tutor, no extra
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        // Search should proceed without additional tax payment
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

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
        harness.paySearchTax(player2);
        harness.passBothPriorities(); // resolve

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player2, 0);

        // Verify paid status is recorded
        assertThat(gd.paidSearchTaxPermanentIds.get(player2.getId())).isNotEmpty();

        // Advance through an actual turn boundary so the engine must expire the payment.
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        // Now player2 should need to pay again — no mana means prevented
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4); // no extra for Arbiter
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("prevented by Leonin Arbiter"));
    }

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

        // Search proceeds normally — no Arbiter tax
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(entry -> entry.contains("Leonin Arbiter"));
    }

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
        harness.paySearchTax(player2); // pays {4} for both Arbiters
        harness.passBothPriorities(); // resolve

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("pays {4} for Leonin Arbiter search tax"));
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

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("prevented by Leonin Arbiter"));
    }

    @Test
    @DisplayName("Leonin Arbiter prevents Terramorphic Expanse search without mana")
    void preventsActivatedAbilitySearchWithoutMana() {
        harness.addToBattlefield(player1, new LeoninArbiter());
        harness.addToBattlefield(player1, new TerramorphicExpanse());

        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        harness.activateAbility(player1, 1, null, null); // activate Terramorphic Expanse (index 1)
        harness.passBothPriorities();

        // Search is prevented — no mana to pay Arbiter tax
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("prevented by Leonin Arbiter"));
    }

    @Test
    @DisplayName("Leonin Arbiter allows Terramorphic Expanse search when player pays tax during priority")
    void allowsActivatedAbilitySearchWhenPaid() {
        harness.addToBattlefield(player1, new LeoninArbiter());
        harness.addToBattlefield(player1, new TerramorphicExpanse());
        harness.addMana(player1, ManaColor.COLORLESS, 2); // for Arbiter tax

        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        harness.activateAbility(player1, 1, null, null);

        // Pay search tax while ability is on the stack
        harness.paySearchTax(player1);

        harness.passBothPriorities(); // resolve

        // Search proceeds — tax was paid
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("pays {2} for Leonin Arbiter search tax"));
    }

    @Test
    @DisplayName("Paying search tax retains priority (does not pass)")
    void paySearchTaxRetainsPriority() {
        harness.addToBattlefield(player1, new LeoninArbiter());

        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 6); // 4 for Tutor + 2 for Arbiter

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);

        // Pay search tax
        harness.paySearchTax(player2);

        // Player still has priority — priorityPassedBy should not include player2
        assertThat(gd.priorityPassedBy).doesNotContain(player2.getId());
    }

    @Test
    @DisplayName("A player can pay for one Arbiter even when unable to pay for both")
    void canIgnoreOneOfTwoArbiters() {
        harness.addToBattlefield(player1, new LeoninArbiter());
        harness.addToBattlefield(player2, new LeoninArbiter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.paySearchTax(player1);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.paidSearchTaxPermanentIds.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({TurnToFrog.class})
    @DisplayName("An Arbiter that has lost all abilities does not prevent searching")
    void abilityRemovalRestoresSearching() {
        harness.addToBattlefield(player1, new LeoninArbiter());
        harness.setHand(player1, List.of(new TurnToFrog(), new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);
        setupLibrary(player1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Leonin Arbiter"));
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("Paying for one player does not allow the other player to search")
    void paymentAppliesOnlyToPayingPlayer() {
        harness.addToBattlefield(player1, new LeoninArbiter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.paySearchTax(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("An Arbiter entering after payment imposes a new restriction")
    void newArbiterRequiresNewPayment() {
        harness.addToBattlefield(player1, new LeoninArbiter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.paySearchTax(player1);
        harness.addToBattlefield(player2, new LeoninArbiter());
        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        setupLibrary(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    private void setupLibrary(com.github.laxika.magicalvibes.model.Player player) {
        harness.setLibrary(player, List.of(new Plains(), new Swamp(), new GrizzlyBears(), new GrizzlyBears()));
    }
}
