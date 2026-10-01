package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
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

@CardUsed({GalepowderMage.class, HillcomberGiant.class})
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
        harness.addToBattlefield(player2, new HillcomberGiant());
        Permanent giant = gd.playerBattlefields.get(player2.getId()).getFirst();

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
        harness.addToBattlefield(player2, new HillcomberGiant());
        Permanent giant = gd.playerBattlefields.get(player2.getId()).getFirst();

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
        harness.addToBattlefield(player1, new HillcomberGiant());
        Permanent giant = findPermanent(player1, "Hillcomber Giant");

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

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
