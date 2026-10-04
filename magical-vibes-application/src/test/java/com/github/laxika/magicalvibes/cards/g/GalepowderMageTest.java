package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GalepowderMage.class, HillcomberGiant.class, NamelessInversion.class})
class GalepowderMageTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues the attack trigger for target selection")
    void attackQueuesTargetSelection() {
        addCreatureReady(player1, new GalepowderMage());
        harness.addToBattlefield(player2, new HillcomberGiant());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Resolving the attack trigger exiles the target creature")
    void attackTriggerExilesTarget() {
        addCreatureReady(player1, new GalepowderMage());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities(); // resolve attack trigger

        harness.assertNotOnBattlefield(player2, "Hillcomber Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hillcomber Giant"));
    }

    @Test
    @DisplayName("Exiled creature returns at the next end step under its owner's control")
    void exiledCreatureReturnsAtEndStep() {
        addCreatureReady(player1, new GalepowderMage());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities(); // resolve attack trigger

        harness.assertNotOnBattlefield(player2, "Hillcomber Giant");

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Hillcomber Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Hillcomber Giant"));
    }

    @Test
    @DisplayName("Can exile another creature its own controller controls")
    void canExileOwnOtherCreature() {
        addCreatureReady(player1, new GalepowderMage());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities(); // resolve attack trigger

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hillcomber Giant"));

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Hillcomber Giant");
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        Permanent mage = addCreatureReady(player1, new GalepowderMage());
        Permanent giant = addCreatureReady(player1, new HillcomberGiant());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactly(giant.getId());
        assertThat(targetChoice.validPermanentIds()).doesNotContain(mage.getId());
    }

    @Test
    @DisplayName("Another Galepowder Mage is a legal target")
    void canTargetAnotherMage() {
        Permanent attacker = addCreatureReady(player1, new GalepowderMage());
        Permanent otherMage = harness.addToBattlefieldAndReturn(player1, new GalepowderMage());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(otherMage.getId());
        harness.handlePermanentChosen(player1, otherMage.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker).doesNotContain(otherMage);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(otherMage.getCard());
    }

    @Test
    @DisplayName("A stolen creature returns to its owner rather than its previous controller")
    void stolenCreatureReturnsToOwner() {
        addCreatureReady(player1, new GalepowderMage());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());
        gd.stolenCreatures.put(giant.getId(), player2.getId());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(giant.getCard());
        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Hillcomber Giant");
        Permanent returned = findPermanent(player2, "Hillcomber Giant");
        assertThat(returned.getId()).isNotEqualTo(giant.getId());
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger and delayed return still work after the Mage dies")
    void sourceLeavingDoesNotStopExileOrReturn() {
        Permanent mage = addCreatureReady(player1, new GalepowderMage());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, mage.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Galepowder Mage");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(giant.getCard());
        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Hillcomber Giant");
    }

    @Test
    @DisplayName("The return uses the stack and does not happen before the end step")
    void returnIsDelayedTriggeredAbility() {
        addCreatureReady(player1, new GalepowderMage());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.assertNotOnBattlefield(player2, "Hillcomber Giant");
        harness.passUntil(TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player2, "Hillcomber Giant");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Hillcomber Giant");
    }

    private void advanceToEndStep() {
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
