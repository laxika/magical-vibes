package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GavonyIronwright;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeedThePack.class, GavonyIronwright.class})
class FeedThePackTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting and sacrificing a 1/4 creature creates four 2/2 Wolf tokens")
    void acceptCreatesWolvesEqualToToughness() {
        harness.addToBattlefield(player1, new FeedThePack());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GavonyIronwright());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Advance to end step — trigger queues MayEffect onto the stack
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);

        // Resolve the triggered ability — MayEffect presents the may choice
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        // Accept — now must choose a creature to sacrifice
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        // Gavony Ironwright was sacrificed
        harness.assertNotOnBattlefield(player1, "Gavony Ironwright");
        harness.assertInGraveyard(player1, "Gavony Ironwright");

        // Four 2/2 green Wolf tokens were created (toughness 4)
        var wolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .toList();
        assertThat(wolves).hasSize(4);
        assertThat(wolves).allSatisfy(w -> {
            assertThat(w.getCard().getPower()).isEqualTo(2);
            assertThat(w.getCard().getToughness()).isEqualTo(2);
            assertThat(w.getCard().isToken()).isTrue();
        });
    }

    @Test
    @DisplayName("Declining the trigger leaves the creature alive and creates no tokens")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new FeedThePack());
        harness.addToBattlefield(player1, new GavonyIronwright());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to end step → trigger queued
        harness.passBothPriorities(); // resolve trigger → may choice

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, false);

        // Gavony Ironwright still on the battlefield, no Wolf tokens created
        harness.assertOnBattlefield(player1, "Gavony Ironwright");
        harness.assertNotOnBattlefield(player1, "Wolf");
    }

    @Test
    @DisplayName("Accepting with no creatures to sacrifice creates no tokens")
    void acceptWithNoCreaturesDoesNothing() {
        harness.addToBattlefield(player1, new FeedThePack());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to end step → trigger queued
        harness.passBothPriorities(); // resolve trigger → may choice

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // No creature to sacrifice — no choice prompt, no Wolf tokens
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Wolf");
    }

    @Test
    @DisplayName("Only the controller's end step triggers Feed the Pack")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new FeedThePack());
        harness.addToBattlefield(player1, new GavonyIronwright());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Gavony Ironwright");
        harness.assertNotOnBattlefield(player1, "Wolf");
    }

    @Test
    @DisplayName("The sacrificed creature's modified toughness determines the number of Wolves")
    void usesEffectiveToughness() {
        harness.addToBattlefield(player1, new FeedThePack());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GavonyIronwright());
        creature.setToughnessModifier(2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))).hasSize(6);
        harness.assertInGraveyard(player1, "Gavony Ironwright");
    }

    @Test
    @DisplayName("Tokens and opposing creatures cannot be sacrificed to Feed the Pack")
    void excludesTokensAndOpposingCreatures() {
        harness.addToBattlefield(player1, new FeedThePack());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GavonyIronwright());
        harness.addToBattlefield(player2, new GavonyIronwright());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))).hasSize(4);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))).hasSize(4);
        harness.assertOnBattlefield(player2, "Gavony Ironwright");
    }
}
