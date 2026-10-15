package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Myrsmith.class, Spellbook.class, GrizzlyBears.class, Memnite.class})
class MyrsmithTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact cast puts the trigger on the stack before the payment choice")
    void artifactCastTriggersMayPrompt() {
        harness.addToBattlefield(player1, new Myrsmith());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting pays {1} and creates a 1/1 Myr artifact creature token")
    void acceptPaysAndCreatesToken() {
        harness.addToBattlefield(player1, new Myrsmith());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.passBothPriorities();

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();

        // A Myr token should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Myr")
                        && p.getCard().isToken()
                        && p.getCard().hasType(CardType.CREATURE)
                        && p.getCard().hasType(CardType.ARTIFACT)
                        && p.getCard().getColors().isEmpty()
                        && p.getCard().getSubtypes().contains(CardSubtype.MYR)
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);

        // Mana should have been spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining may ability does not create token or spend mana")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new Myrsmith());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Myrsmith"));

        // No Myr token created
        harness.assertNotOnBattlefield(player1, "Myr");

        // Mana not spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-artifact spell does not trigger Myrsmith")
    void nonArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Myrsmith());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting artifact does not trigger Myrsmith")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Myrsmith());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Accepting with no mana treats as decline")
    void cannotPayTreatsAsDecline() {
        harness.addToBattlefield(player1, new Myrsmith());
        harness.setHand(player1, List.of(new Spellbook()));
        // No mana added — cannot pay {1}

        harness.castArtifact(player1, 0);

        // May prompt fires
        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        // Accept, but cannot pay
        harness.handleMayAbilityChosen(player1, true);

        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Myrsmith"));

        // No token created
        harness.assertNotOnBattlefield(player1, "Myr");
    }

    @Test
    @DisplayName("Artifact creature cast triggers and colored mana can pay the generic cost")
    void artifactCreatureCastAllowsColoredPayment() {
        harness.addToBattlefield(player1, new Myrsmith());
        harness.setHand(player1, List.of(new Memnite()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Myr");
        harness.assertNotOnBattlefield(player1, "Memnite");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }
}
