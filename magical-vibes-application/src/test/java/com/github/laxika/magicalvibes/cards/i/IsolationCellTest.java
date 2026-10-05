package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.p.PhyrexiasCore;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IsolationCell.class, GrizzlyBears.class, Shock.class, SuntailHawk.class, PhyrexiasCore.class, WitchbaneOrb.class})
class IsolationCellTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers when opponent casts a creature spell")
    void triggersOnOpponentCreatureSpell() {
        harness.addToBattlefield(player1, new IsolationCell());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        // Triggered ability should be on the stack (on top of the creature spell)
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Isolation Cell");
    }

    @Test
    @DisplayName("Does NOT trigger when opponent casts a non-creature spell")
    void doesNotTriggerOnOpponentNonCreatureSpell() {
        harness.addToBattlefield(player1, new IsolationCell());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        // Only the Shock spell on the stack — no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("Does NOT trigger when controller casts a creature spell")
    void doesNotTriggerOnControllerCreatureSpell() {
        harness.addToBattlefield(player1, new IsolationCell());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // Only the creature spell on the stack — no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent with mana is prompted to pay or lose life")
    void opponentWithManaIsPrompted() {
        setupOpponentCastsCreatureWithMana();

        // Resolve the triggered ability — should prompt the opponent
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent accepts pay — mana is spent, no life loss")
    void opponentPaysManaNoLifeLoss() {
        setupOpponentCastsCreatureWithMana();
        harness.passBothPriorities(); // resolve triggered ability

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        // No life loss
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);

        // Mana was spent
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent declines to pay — loses 2 life")
    void opponentDeclinesToPayLosesLife() {
        setupOpponentCastsCreatureWithMana();
        harness.passBothPriorities(); // resolve triggered ability

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Auto-loses 2 life when opponent has no mana to pay")
    void autoLosesLifeWithNoMana() {
        harness.addToBattlefield(player1, new IsolationCell());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);

        // No mana left after casting — resolve triggered ability
        harness.passBothPriorities();

        // No prompt — it was automatic
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Life loss from not paying can reduce opponent to 0 or below")
    void lifeLossCanKill() {
        harness.addToBattlefield(player1, new IsolationCell());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player2, 1);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        // No mana left — auto life loss
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(-1);
    }

    @Test
    @DisplayName("Multiple Isolation Cells each trigger independently")
    void multipleTriggerIndependently() {
        harness.addToBattlefield(player1, new IsolationCell());
        harness.addToBattlefield(player1, new IsolationCell());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);

        // Two triggered abilities on the stack (plus the creature spell)
        long triggeredCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredCount).isEqualTo(2);

        // Resolve first triggered ability — opponent has no mana, auto life loss
        harness.passBothPriorities();

        // Resolve second triggered ability — opponent still has no mana, auto life loss
        harness.passBothPriorities();

        // Lost 2 + 2 = 4 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Hexproof does not stop the non-targeting life-loss trigger")
    void affectsOpponentWithHexproof() {
        harness.addToBattlefield(player1, new IsolationCell());
        harness.addToBattlefield(player2, new WitchbaneOrb());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent may generate mana during resolution before choosing to pay")
    void mayActivateManaAbilitiesWhenAskedToPay() {
        harness.addToBattlefield(player1, new IsolationCell());
        harness.addToBattlefield(player2, new PhyrexiasCore());
        harness.addToBattlefield(player2, new PhyrexiasCore());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.tapPermanent(player2, 0);
        harness.tapPermanent(player2, 1);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Trigger still resolves after Isolation Cell leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        setupOpponentCastsCreatureWithMana();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature entering without being cast does not trigger Isolation Cell")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player1, new IsolationCell());

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    /**
     * Sets up: player1 has Isolation Cell on battlefield, player2 casts a creature spell
     * with extra mana available to pay. After this, the triggered ability is on the stack.
     */
    private void setupOpponentCastsCreatureWithMana() {
        harness.addToBattlefield(player1, new IsolationCell());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);

        // Sanity check: triggered ability is on the stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }
}
