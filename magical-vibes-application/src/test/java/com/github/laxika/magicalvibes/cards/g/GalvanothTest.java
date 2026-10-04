package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Slagstorm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Galvanoth.class, GrizzlyBears.class, KuldothaRebirth.class, Pyroclasm.class, Shock.class, Slagstorm.class})
class GalvanothTest extends BaseCardTest {

    // ===== Upkeep trigger — may look prompt =====

    @Test
    @DisplayName("Upkeep prompts with may ability to look at top card")
    void upkeepPromptsMayAbilityToLook() {
        harness.addToBattlefield(player1, new Galvanoth());

        advanceToUpkeep(player1);
        // MayEffect goes on the stack — resolve it to get prompt
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    // ===== Declining to look =====

    @Test
    @DisplayName("Declining to look does nothing")
    void decliningToLookDoesNothing() {
        harness.addToBattlefield(player1, new Galvanoth());
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, false); // decline to look

        // Card is still on top of library
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    // ===== Non-instant/sorcery on top =====

    @Test
    @DisplayName("Looking at a creature card on top does not offer to cast")
    void nonInstantSorceryOnTopDoesNotOfferCast() {
        harness.addToBattlefield(player1, new Galvanoth());
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, true); // accept look — inner resolves inline, sees creature on top, no second may prompt

        // Card is still on top of library
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    // ===== Cast non-targeted sorcery from top =====

    @Test
    @DisplayName("Casting Pyroclasm from library deals 2 damage to all creatures without paying mana")
    void castNonTargetedSorceryFromLibrary() {
        harness.addToBattlefield(player1, new Galvanoth());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        Card pyroclasm = new Pyroclasm();
        gd.playerDecks.get(player1.getId()).addFirst(pyroclasm);

        advanceToUpkeep(player1);

        // MayEffect goes on stack — resolve it to get prompt
        harness.passBothPriorities();

        // First may: look at top card
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true); // inner resolves inline → sees Pyroclasm → second may

        // Second may: cast Pyroclasm
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true); // cast Pyroclasm

        // Pyroclasm is on the stack
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getCard()).isSameAs(pyroclasm);

        // Card removed from library
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(pyroclasm);

        // Resolve the spell
        harness.passBothPriorities();

        // Grizzly Bears (2/2) should be dead from 2 damage
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Pyroclasm goes to graveyard
        harness.assertInGraveyard(player1, "Pyroclasm");

        // No mana was spent
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    // ===== Cast targeted instant from top =====

    @Test
    @DisplayName("Casting Shock from library deals 2 damage to chosen target")
    void castTargetedInstantFromLibrary() {
        harness.addToBattlefield(player1, new Galvanoth());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        advanceToUpkeep(player1);

        // MayEffect goes on stack — resolve it to get prompt
        harness.passBothPriorities();

        // First may: look at top card
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true); // inner resolves inline → sees Shock → second may

        // Second may: cast Shock
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true); // cast Shock

        // Now should be prompted for target
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose Grizzly Bears as target
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        // Shock is on the stack targeting Bears
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getCard()).isSameAs(shock);

        // Resolve the spell
        harness.passBothPriorities();

        // Grizzly Bears should be dead from 2 damage
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Shock goes to graveyard
        harness.assertInGraveyard(player1, "Shock");
    }

    // ===== Declining to cast =====

    @Test
    @DisplayName("Declining to cast leaves the card on top of library")
    void decliningToCastLeavesCardOnTop() {
        harness.addToBattlefield(player1, new Galvanoth());
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, true); // accept look — inner resolves inline → sees Shock → second may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false); // decline to cast

        // Card is still on top of library
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Empty library — looking does nothing")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new Galvanoth());
        gd.playerDecks.get(player1.getId()).clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, true); // accept look — inner resolves inline, library empty, nothing happens

        // No errors, game continues normally
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    // ===== Cast from library counts as spell cast =====

    @Test
    @DisplayName("Casting from library increments spells-cast-this-turn counter")
    void castFromLibraryCountsAsSpellCast() {
        harness.addToBattlefield(player1, new Galvanoth());
        Card pyroclasm = new Pyroclasm();
        gd.playerDecks.get(player1.getId()).addFirst(pyroclasm);
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, true); // accept look — inner resolves inline → second may prompt
        harness.handleMayAbilityChosen(player1, true); // cast Pyroclasm

        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
    }

    // ===== Only triggers on controller's upkeep =====

    @Test
    @DisplayName("Does not trigger on opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new Galvanoth());

        advanceToUpkeep(player2); // opponent's upkeep
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof Galvanoth);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Looking at a creature on top privately shows its identity to the controller")
    void lookingAtCreatureShowsItOnlyToController() throws Exception {
        harness.addToBattlefield(player1, new Galvanoth());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        List<GameEventEnvelope> events = new ArrayList<>();

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal reveal
                        && reveal.zone() == GameEventFact.RevealZone.LIBRARY
                        && reveal.subjectPlayerId().equals(player1.getId()))
                .isNotEmpty()
                .allSatisfy(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::cardId)
                            .containsExactly(bears.getId());
                    assertThat(event.audience().playerIds()).containsExactly(player1.getId());
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("A spell with a mandatory artifact sacrifice cannot be cast without an artifact")
    void cannotCastWithoutMandatorySacrifice() {
        harness.addToBattlefield(player1, new Galvanoth());
        Card rebirth = new KuldothaRebirth();
        harness.setLibrary(player1, List.of(rebirth));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(rebirth);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == rebirth);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isZero();
    }

    @Test
    @DisplayName("A modal sorcery's mode is chosen during casting before opponents can respond")
    void modalSpellChoosesModeBeforeGoingOnStack() {
        harness.addToBattlefield(player1, new Galvanoth());
        Card slagstorm = new Slagstorm();
        harness.setLibrary(player1, List.of(slagstorm));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "Slagstorm deals 3 damage to each player");
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == slagstorm);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Galvanoth");
        harness.assertInGraveyard(player1, "Slagstorm");
    }
}
