package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PainfulQuandary.class, CarapaceForger.class, Memnite.class})
class PainfulQuandaryTest extends BaseCardTest {

    @Test
    @DisplayName("Does NOT trigger when controller casts a spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new PainfulQuandary());
        harness.castFromHand(player1, new CarapaceForger(), "{1}{G}");

        // No triggered ability — only the creature spell on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Triggers when opponent casts a spell — puts triggered ability on stack")
    void triggersOnOpponentSpell() {
        harness.addToBattlefield(player1, new PainfulQuandary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new CarapaceForger(), "{1}{G}");

        // Triggered ability should be on the stack (on top of the creature spell)
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Painful Quandary");
    }

    @Test
    @DisplayName("Opponent with cards in hand is prompted to discard or lose life")
    void opponentWithCardsIsPrompted() {
        setupOpponentCastsSpellWithCardsInHand();

        // Resolve the triggered ability — should prompt the opponent
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent accepts discard — prompted to choose card to discard")
    void opponentAcceptsDiscardPrompted() {
        setupOpponentCastsSpellWithCardsInHand();
        harness.passBothPriorities(); // resolve triggered ability

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent discards a card — no life loss")
    void opponentDiscardsNoLifeLoss() {
        setupOpponentCastsSpellWithCardsInHand();
        harness.passBothPriorities(); // resolve triggered ability

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0); // discard the first card

        // No life loss
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);

        // The card was discarded
        harness.assertInGraveyard(player2, "Memnite");
    }

    @Test
    @DisplayName("Opponent declines discard — loses 5 life")
    void opponentDeclinesDiscardLosesLife() {
        setupOpponentCastsSpellWithCardsInHand();
        harness.passBothPriorities(); // resolve triggered ability

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("Opponent declines discard — hand is untouched")
    void opponentDeclinesDiscardHandUntouched() {
        setupOpponentCastsSpellWithCardsInHand();
        harness.passBothPriorities(); // resolve triggered ability

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Auto-loses 5 life when opponent has empty hand")
    void autoLosesLifeWithEmptyHand() {
        harness.addToBattlefield(player1, new PainfulQuandary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player2, new CarapaceForger(), "{1}{G}");

        // Hand is now empty (creature was cast from hand)
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        // Resolve the triggered ability — auto life loss since hand is empty
        harness.passBothPriorities();

        // No prompt — it was automatic
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to discard")
                && log.contains("loses 5 life"));
    }

    @Test
    @DisplayName("Multiple Painful Quandaries each trigger independently")
    void multipleTriggerIndependently() {
        harness.addToBattlefield(player1, new PainfulQuandary());
        harness.addToBattlefield(player1, new PainfulQuandary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CarapaceForger(), new Memnite(), new Memnite()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);

        // Two triggered abilities on the stack (plus the creature spell)
        long triggeredCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredCount).isEqualTo(2);

        // Resolve first triggered ability — opponent discards
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0); // discard first card

        // Resolve second triggered ability — opponent declines
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        // Lost 5 life from declining the second trigger
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);

        // One card was discarded
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Life loss from declining can reduce opponent to 0 or below")
    void lifeLossCanKill() {
        harness.addToBattlefield(player1, new PainfulQuandary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player2, 3);

        harness.castFromHand(player2, new CarapaceForger(), "{1}{G}");

        // Hand is empty — auto life loss
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(-2);
    }

    @Test
    @DisplayName("Artifact creature spells also trigger the ability")
    void artifactCreatureSpellTriggers() {
        harness.addToBattlefield(player1, new PainfulQuandary());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new Memnite(), "{0}");
        harness.setLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Memnite");
    }

    @Test
    @DisplayName("Discarding the last card to one trigger leaves life loss for the other")
    void multipleTriggersRecheckHandAtResolution() {
        harness.addToBattlefield(player1, new PainfulQuandary());
        harness.addToBattlefield(player1, new PainfulQuandary());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new CarapaceForger(), new Memnite()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertLife(player2, 15);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed(ObstinateBaloth.class)
    @DisplayName("Discard to an opponent's Quandary applies Obstinate Baloth's replacement")
    void discardIsCausedByOpponentsAbility() {
        harness.addToBattlefield(player1, new PainfulQuandary());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new CarapaceForger(), new ObstinateBaloth()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Obstinate Baloth");
        harness.assertNotInGraveyard(player2, "Obstinate Baloth");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 24);
    }

    /**
     * Sets up: player1 has Painful Quandary on battlefield, player2 casts a spell
     * with additional cards remaining in hand. After this, the triggered ability
     * is on the stack (not yet resolved).
     */
    private void setupOpponentCastsSpellWithCardsInHand() {
        harness.addToBattlefield(player1, new PainfulQuandary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CarapaceForger(), new Memnite()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        // Sanity check: triggered ability is on the stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }
}
