package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PoisonbellyOgre.class, GhostWarden.class, GruulSignet.class})
class PoisonbellyOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Creature entry requires no target choice and life loss waits for resolution")
    void entryTriggerIsMandatoryAndNonTargeting() {
        harness.addToBattlefield(player1, new PoisonbellyOgre());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new GhostWarden());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An existing Ogre triggers for a second Ogre entering")
    void existingOgreTriggersForAnotherOgre() {
        harness.addToBattlefield(player1, new PoisonbellyOgre());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new PoisonbellyOgre());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Its controller loses 1 life when another creature enters")
    void creatureControllerLosesLife() {
        harness.addToBattlefield(player1, new PoisonbellyOgre());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GhostWarden(), "{1}{W}");
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Triggers when a creature enters under its controller's control")
    void triggersForControllerCreature() {
        harness.addToBattlefield(player1, new PoisonbellyOgre());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new GhostWarden(), "{1}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Does not trigger when Poisonbelly Ogre enters")
    void doesNotTriggerForSelf() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PoisonbellyOgre(), "{4}{B}");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger when a noncreature permanent enters")
    void doesNotTriggerForNoncreaturePermanent() {
        harness.addToBattlefield(player1, new PoisonbellyOgre());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new GruulSignet(), "{2}");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each Poisonbelly Ogre triggers for the same creature entering")
    void eachPoisonbellyOgreTriggers() {
        harness.addToBattlefield(player1, new PoisonbellyOgre());
        harness.addToBattlefield(player1, new PoisonbellyOgre());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GhostWarden(), "{1}{W}");
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }
}
