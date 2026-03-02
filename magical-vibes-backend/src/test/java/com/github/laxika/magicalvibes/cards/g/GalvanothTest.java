package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GalvanothTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has upkeep may-look-and-cast effect")
    void hasCorrectProperties() {
        Galvanoth card = new Galvanoth();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(may.wrapped()).isInstanceOf(CastTopOfLibraryWithoutPayingManaCostEffect.class);
        CastTopOfLibraryWithoutPayingManaCostEffect effect = (CastTopOfLibraryWithoutPayingManaCostEffect) may.wrapped();
        assertThat(effect.castableTypes()).containsExactlyInAnyOrder(CardType.INSTANT, CardType.SORCERY);
    }

    // ===== Upkeep trigger — may look prompt =====

    @Test
    @DisplayName("Upkeep prompts with may ability to look at top card")
    void upkeepPromptsMayAbilityToLook() {
        harness.addToBattlefield(player1, new Galvanoth());

        advanceToUpkeep(player1);
        // MayEffect goes directly into pendingMayAbilities — already awaiting choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    // ===== Declining to look =====

    @Test
    @DisplayName("Declining to look does nothing")
    void decliningToLookDoesNothing() {
        harness.addToBattlefield(player1, new Galvanoth());
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        advanceToUpkeep(player1);
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
        harness.handleMayAbilityChosen(player1, true); // accept look
        // Wrapped effect goes on stack
        harness.passBothPriorities(); // resolve → sees creature on top, no second may prompt

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

        // First may: look at top card
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);

        // Wrapped effect goes on stack — resolve it
        harness.passBothPriorities();

        // Second may: cast Pyroclasm
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true); // cast Pyroclasm

        // Pyroclasm is on the stack
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getCard()).isSameAs(pyroclasm);

        // Card removed from library
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(pyroclasm);

        // Resolve the spell
        harness.passBothPriorities();

        // Grizzly Bears (2/2) should be dead from 2 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Pyroclasm goes to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Pyroclasm"));

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

        // First may: look at top card
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);

        // Wrapped effect goes on stack — resolve it
        harness.passBothPriorities();

        // Second may: cast Shock
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true); // cast Shock

        // Now should be prompted for target
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose Grizzly Bears as target
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        // Shock is on the stack targeting Bears
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getCard()).isSameAs(shock);

        // Resolve the spell
        harness.passBothPriorities();

        // Grizzly Bears should be dead from 2 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Shock goes to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    // ===== Declining to cast =====

    @Test
    @DisplayName("Declining to cast leaves the card on top of library")
    void decliningToCastLeavesCardOnTop() {
        harness.addToBattlefield(player1, new Galvanoth());
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        advanceToUpkeep(player1);
        harness.handleMayAbilityChosen(player1, true); // accept look
        harness.passBothPriorities(); // resolve → sees Shock → second may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
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
        harness.handleMayAbilityChosen(player1, true); // accept look
        harness.passBothPriorities(); // resolve → library empty, nothing happens

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
        gd.spellsCastThisTurn.put(player1.getId(), 0);

        advanceToUpkeep(player1);
        harness.handleMayAbilityChosen(player1, true); // accept look
        harness.passBothPriorities(); // resolve → second may prompt
        harness.handleMayAbilityChosen(player1, true); // cast Pyroclasm

        assertThat(gd.spellsCastThisTurn.get(player1.getId())).isEqualTo(1);
    }

    // ===== Only triggers on controller's upkeep =====

    @Test
    @DisplayName("Does not trigger on opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new Galvanoth());

        advanceToUpkeep(player2); // opponent's upkeep
        // If the trigger fired, there would be a may prompt
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
