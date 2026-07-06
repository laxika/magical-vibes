package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiligentExcavatorTest extends BaseCardTest {

    // ===== Artifact spell triggers target selection =====

    @Test
    @DisplayName("Casting an artifact triggers target player selection")
    void artifactSpellTriggersTargetSelection() {
        harness.addToBattlefield(player1, new DiligentExcavator());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    // ===== Mill resolves correctly targeting opponent =====

    @Test
    @DisplayName("Choosing opponent as target mills two cards from their library")
    void millOpponentLibrary() {
        harness.addToBattlefield(player1, new DiligentExcavator());
        harness.setHand(player1, List.of(new Spellbook()));

        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.castArtifact(player1, 0);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    // ===== Can target self =====

    @Test
    @DisplayName("Can target yourself to mill your own library")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new DiligentExcavator());
        harness.setHand(player1, List.of(new Spellbook()));

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.castArtifact(player1, 0);

        // Choose self as target
        harness.handlePermanentChosen(player1, player1.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    // ===== Legendary spell triggers =====

    @Test
    @DisplayName("Casting a legendary creature triggers target player selection")
    void legendarySpellTriggersTargetSelection() {
        harness.addToBattlefield(player1, new DiligentExcavator());
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    // ===== Non-historic does not trigger =====

    @Test
    @DisplayName("Casting a non-historic creature does not trigger the ability")
    void nonHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new DiligentExcavator());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Only the creature spell should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Opponent's historic spell does not trigger =====

    @Test
    @DisplayName("Opponent casting an artifact does not trigger controller's Diligent Excavator")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new DiligentExcavator());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        // Only the artifact spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    // ===== Triggered ability resolves before the spell (LIFO) =====

    @Test
    @DisplayName("Triggered ability resolves before the artifact spell (LIFO stack order)")
    void triggeredAbilityResolvesBeforeArtifact() {
        harness.addToBattlefield(player1, new DiligentExcavator());
        harness.setHand(player1, List.of(new Spellbook()));

        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castArtifact(player1, 0);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the triggered ability first (top of stack, LIFO)
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Opponent's library should be milled
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);

        // Resolve the artifact spell
        harness.passBothPriorities();

        gd = harness.getGameData();
        // Spellbook should now be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spellbook"));
    }
}
