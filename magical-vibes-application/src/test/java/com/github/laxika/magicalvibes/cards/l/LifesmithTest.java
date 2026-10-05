package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.a.AlphaTyrranax;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lifesmith.class, AccordersShield.class, AlphaTyrranax.class, Memnite.class})
class LifesmithTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact puts the trigger on the stack before the payment choice")
    void artifactCastTriggersMayPrompt() {
        harness.addToBattlefield(player1, new Lifesmith());

        harness.castFromHand(player1, new AccordersShield(), "{0}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting pays {1} and gains 3 life")
    void acceptPaysAndGainsLife() {
        harness.addToBattlefield(player1, new Lifesmith());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new AccordersShield(), "{0}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 3);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Accorder's Shield");

        // Resolve the artifact spell
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);

        // Mana should have been spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining may ability does not gain life or spend mana")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new Lifesmith());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new AccordersShield(), "{0}");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Lifesmith"));

        // Mana not spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Non-artifact spell does not trigger Lifesmith")
    void nonArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifesmith());
        harness.castFromHand(player1, new AlphaTyrranax(), "{4}{G}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting artifact does not trigger Lifesmith")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifesmith());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new AccordersShield(), "{0}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("The trigger resolves without life gain when its controller cannot pay")
    void cannotPayTreatsAsDecline() {
        harness.addToBattlefield(player1, new Lifesmith());

        harness.castFromHand(player1, new AccordersShield(), "{0}");
        assertThat(gd.stack).hasSize(2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.passBothPriorities();

        // May prompt fires
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        // Accept, but cannot pay
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).hasSize(1);

        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Lifesmith"));
    }
    @Test
    @DisplayName("Artifact creatures trigger Lifesmith and colored mana pays the generic cost")
    void artifactCreatureTriggersAndColoredManaPays() {
        harness.addToBattlefield(player1, new Lifesmith());
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castFromHand(player1, new Memnite(), "{0}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Memnite");
    }

    @Test
    @DisplayName("Each Lifesmith creates an independent trigger with its own payment choice")
    void multipleLifesmithsHaveIndependentPaymentChoices() {
        harness.addToBattlefield(player1, new Lifesmith());
        harness.addToBattlefield(player1, new Lifesmith());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new Memnite(), "{0}");

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, lifeBefore + 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertLife(player1, lifeBefore + 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Memnite");
    }
}
