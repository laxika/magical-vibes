package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Embersmith.class, GrizzlyBears.class, Spellbook.class, SuntailHawk.class, GalvanicBlast.class, Memnite.class})
class EmbersmithTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact requires a target before the payment decision")
    void artifactCastRequiresTargetBeforePayment() {
        harness.addToBattlefield(player1, new Embersmith());

        harness.castFromHand(player1, new Spellbook(), "{0}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Accepting pays {1} and deals 1 damage to target creature, killing a 1/1")
    void acceptPaysDamageToCreature() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.addToBattlefield(player2, new SuntailHawk());
        UUID targetId = harness.getPermanentId(player2, "Suntail Hawk");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, new Spellbook(), "{0}");

        // Should be prompting for target selection
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose the creature target
        harness.handlePermanentChosen(player1, targetId);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Embersmith"));

        // Payment is offered only when the targeted trigger resolves.
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Suntail Hawk (1/1) should be destroyed by 1 damage
        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");

        // Mana should have been spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Accepting pays {1} and deals 1 damage to target player")
    void acceptPaysDamageToPlayer() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new Spellbook(), "{0}");
        // Choose the opponent as the target
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve triggered ability
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Resolve Spellbook
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Declining may ability does not deal damage or spend mana")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Embersmith"));

        // Mana not spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        // No damage dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Non-artifact spell does not trigger Embersmith")
    void nonArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting artifact does not trigger Embersmith")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Embersmith());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Spellbook(), "{0}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Without mana the trigger still targets and resolves without damage")
    void cannotPayTreatsAsDecline() {
        harness.addToBattlefield(player1, new Embersmith());

        harness.castFromHand(player1, new Spellbook(), "{0}");

        // Target selection is required even without mana.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 20);

        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Embersmith"));
    }

    @Test
    @DisplayName("Mana is not spent until the targeted trigger resolves")
    void manaIsNotSpentWhenTriggerIsStacked() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new Spellbook(), "{0}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Embersmith can target its controller")
    void canDamageController() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new Spellbook(), "{0}");

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casting an artifact creature triggers before that creature enters")
    void artifactCreatureCastTriggers() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new Memnite(), "{0}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Memnite");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Memnite");
    }

    @Test
    @DisplayName("An illegal target prevents resolution and the payment choice")
    void removedTargetDoesNotSpendMana() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.addToBattlefield(player2, new SuntailHawk());
        UUID targetId = harness.getPermanentId(player2, "Suntail Hawk");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.handlePermanentChosen(player1, targetId);

        harness.setHand(player2, List.of(new GalvanicBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Suntail Hawk");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }
}
