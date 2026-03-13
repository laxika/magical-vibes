package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LurkingPredatorsTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_OPPONENT_CASTS_SPELL triggered RevealTopCardCreatureToBattlefieldOrMayBottomEffect")
    void hasCorrectEffect() {
        LurkingPredators card = new LurkingPredators();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CASTS_SPELL).getFirst())
                .isInstanceOf(RevealTopCardCreatureToBattlefieldOrMayBottomEffect.class);
    }

    // ===== Trigger fires on opponent's spell =====

    @Test
    @DisplayName("Triggers when opponent casts a spell")
    void triggersOnOpponentSpell() {
        harness.addToBattlefield(player1, new LurkingPredators());
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);

        long triggeredCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Does NOT trigger when controller casts a spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new LurkingPredators());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Creature card revealed — put onto battlefield =====

    @Test
    @DisplayName("Creature card is put onto the battlefield")
    void creatureCardPutOntoBattlefield() {
        harness.addToBattlefield(player1, new LurkingPredators());
        Card creature = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        setupOpponentCastsSpell();

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Creature card is removed from library when put onto battlefield")
    void creatureCardRemovedFromLibrary() {
        harness.addToBattlefield(player1, new LurkingPredators());
        Card creature = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(creature);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        setupOpponentCastsSpell();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("No may ability prompt when creature is revealed")
    void noMayPromptForCreature() {
        harness.addToBattlefield(player1, new LurkingPredators());
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        setupOpponentCastsSpell();
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    // ===== Non-creature card revealed — may put on bottom =====

    @Test
    @DisplayName("Non-creature card prompts may ability to put on bottom")
    void nonCreatureCardPromptsMayAbility() {
        harness.addToBattlefield(player1, new LurkingPredators());
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        setupOpponentCastsSpell();
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accept puts non-creature card on the bottom of library")
    void acceptPutsNonCreatureOnBottom() {
        harness.addToBattlefield(player1, new LurkingPredators());
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        setupOpponentCastsSpell();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Card should be on the bottom of the library
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId())
                .isEqualTo(land.getId());
        // And not on top
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId())
                .isNotEqualTo(land.getId());
    }

    @Test
    @DisplayName("Decline leaves non-creature card on top of library")
    void declineLeavesNonCreatureOnTop() {
        harness.addToBattlefield(player1, new LurkingPredators());
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        setupOpponentCastsSpell();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // Card should still be on top of the library
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId())
                .isEqualTo(land.getId());
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Does nothing when library is empty")
    void doesNothingWhenLibraryEmpty() {
        harness.addToBattlefield(player1, new LurkingPredators());
        gd.playerDecks.get(player1.getId()).clear();

        setupOpponentCastsSpell();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    // ===== Reveal is logged =====

    @Test
    @DisplayName("Reveal is logged in game log")
    void revealIsLogged() {
        harness.addToBattlefield(player1, new LurkingPredators());
        Card creature = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        setupOpponentCastsSpell();
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("reveals") && log.contains("Grizzly Bears"));
    }

    // ===== Helpers =====

    /**
     * Sets up: player2 (opponent) casts a creature spell.
     * After this, the Lurking Predators triggered ability is on the stack.
     */
    private void setupOpponentCastsSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
    }
}
