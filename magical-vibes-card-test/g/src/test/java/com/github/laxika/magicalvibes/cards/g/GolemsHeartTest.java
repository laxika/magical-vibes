package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GolemsHeart.class, AccordersShield.class, CarapaceForger.class, Memnite.class})
class GolemsHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Controller casts artifact spell, accepts may ability, gains 1 life")
    void controllerCastsArtifactAndAccepts() {
        harness.addToBattlefield(player1, new GolemsHeart());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new AccordersShield(), "{0}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        // The choice is made as the trigger resolves.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Resolve the artifact spell
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts artifact spell, declines may ability, no life gain")
    void controllerCastsArtifactAndDeclines() {
        harness.addToBattlefield(player1, new GolemsHeart());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new AccordersShield(), "{0}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Golem's Heart"));

        // Resolve the artifact spell
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent casts artifact spell, controller accepts may ability, gains 1 life")
    void opponentCastsArtifactControllerAccepts() {
        harness.addToBattlefield(player1, new GolemsHeart());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();


        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new AccordersShield(), "{0}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        // Player1 (controller of Golem's Heart) should be prompted
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Resolve the artifact spell
        harness.passBothPriorities(); // resolve artifact spell

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Non-artifact spell does not trigger Golem's Heart")
    void nonArtifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GolemsHeart());
        harness.castFromHand(player1, new CarapaceForger(), "{1}{G}");

        // Should not be awaiting may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Multiple Golem's Hearts each trigger independently")
    void multipleHeartsEachTrigger() {
        harness.addToBattlefield(player1, new GolemsHeart());
        harness.addToBattlefield(player1, new GolemsHeart());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new AccordersShield(), "{0}");

        // Two triggered abilities on the stack (plus the artifact spell)
        long triggeredCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredCount).isEqualTo(2);

        // Resolve all
        harness.passBothPriorities(); // resolve second triggered ability
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve first triggered ability
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve artifact spell

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Golem's Heart does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {

        harness.castFromHand(player1, new AccordersShield(), "{0}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    void artifactCreatureTriggersAndGainsLifeBeforeCreatureResolves() {
        harness.addToBattlefield(player1, new GolemsHeart());
        harness.castFromHand(player1, new Memnite(), "{0}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Memnite");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Memnite");
    }

    @Test
    void heartDoesNotTriggerForItsOwnCast() {
        harness.castFromHand(player1, new GolemsHeart(), "{2}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Golem's Heart");
        harness.assertLife(player1, 20);
    }

    @Test
    void puttingArtifactOntoBattlefieldDoesNotTrigger() {
        harness.addToBattlefield(player1, new GolemsHeart());
        harness.enterBattlefieldAndReturn(player1, new Memnite());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }
}
