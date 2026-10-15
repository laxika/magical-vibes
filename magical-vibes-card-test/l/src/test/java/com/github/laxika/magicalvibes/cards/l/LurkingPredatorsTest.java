package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkingPredators.class, Forest.class, RuneclawBear.class, EliteVanguard.class})
class LurkingPredatorsTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers when opponent casts a spell")
    void triggersOnOpponentSpell() {
        harness.addToBattlefield(player1, new LurkingPredators());
        gd.playerDecks.get(player1.getId()).addFirst(new RuneclawBear());

        setupOpponentCastsSpell();

        long triggeredCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Does NOT trigger when controller casts a spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new LurkingPredators());
        harness.castFromHand(player1, new RuneclawBear(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Creature card is put onto the battlefield")
    void creatureCardPutOntoBattlefield() {
        harness.addToBattlefield(player1, new LurkingPredators());
        Card creature = new RuneclawBear();
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
        Card creature = new RuneclawBear();
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
        gd.playerDecks.get(player1.getId()).addFirst(new RuneclawBear());

        setupOpponentCastsSpell();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Non-creature card prompts may ability to put on bottom")
    void nonCreatureCardPromptsMayAbility() {
        harness.addToBattlefield(player1, new LurkingPredators());
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        setupOpponentCastsSpell();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
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

    @Test
    @DisplayName("Does nothing when library is empty")
    void doesNothingWhenLibraryEmpty() {
        harness.addToBattlefield(player1, new LurkingPredators());
        harness.setLibrary(player1, List.of());

        setupOpponentCastsSpell();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Reveal is logged in game log")
    void revealIsLogged() {
        harness.addToBattlefield(player1, new LurkingPredators());
        Card creature = new RuneclawBear();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        setupOpponentCastsSpell();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("reveals") && log.contains("Runeclaw Bear"));
    }

    @Test
    @CardUsed(Divination.class)
    @DisplayName("Triggers for a noncreature spell and resolves before that spell")
    void triggersBeforeNonCreatureSpellResolves() {
        harness.addToBattlefield(player1, new LurkingPredators());
        harness.setLibrary(player1, List.of(new RuneclawBear(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new Divination(), "{2}{U}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(Divination.class);
        harness.assertNotInGraveyard(player2, "Divination");
    }

    @Test
    @CardUsed(SoulWarden.class)
    @DisplayName("A creature put onto the battlefield triggers creature entry abilities")
    void creatureEntryTriggersAbilitiesWithoutBeingCast() {
        harness.addToBattlefield(player1, new LurkingPredators());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setLibrary(player1, List.of(new RuneclawBear(), new Forest()));
        harness.setLife(player1, 20);
        setupOpponentCastsSpell();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Each copy reveals the current top card when its trigger resolves")
    void multipleCopiesRevealSuccessiveCards() {
        harness.addToBattlefield(player1, new LurkingPredators());
        harness.addToBattlefield(player1, new LurkingPredators());
        Card first = new RuneclawBear();
        Card second = new EliteVanguard();
        harness.setLibrary(player1, List.of(first, second, new Forest()));
        setupOpponentCastsSpell();

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(first.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(second);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(second.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Bottoming the only card leaves it in the library")
    void bottomingOnlyCardKeepsLibraryIntact() {
        harness.addToBattlefield(player1, new LurkingPredators());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        setupOpponentCastsSpell();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    /**
     * Sets up: player2 (opponent) casts a creature spell.
     * After this, the Lurking Predators triggered ability is on the stack.
     */
    private void setupOpponentCastsSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new EliteVanguard(), "{W}");
    }
}
