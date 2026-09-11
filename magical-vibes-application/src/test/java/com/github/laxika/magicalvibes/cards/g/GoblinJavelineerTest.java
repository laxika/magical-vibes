package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinJavelineer.class, GrizzlyBears.class})
class GoblinJavelineerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked queues the blocking creature for target selection")
    void becomingBlockedQueuesTargetSelection() {
        Permanent javelineer = addReadyJavelineer(player1);
        javelineer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(blocker.getId());
    }

    @Test
    @DisplayName("Resolving the trigger deals 1 damage to the chosen blocker")
    void resolvingTriggerDealsDamageToChosenBlocker() {
        Permanent javelineer = addReadyJavelineer(player1);
        javelineer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.handlePermanentChosen(player1, blocker.getId());

        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Multiple blockers create one trigger and only the chosen blocker is damaged")
    void multipleBlockersCreateOneTargetedTrigger() {
        Permanent javelineer = addReadyJavelineer(player1);
        javelineer.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(firstBlocker.getId(), secondBlocker.getId());
        harness.handlePermanentChosen(player1, secondBlocker.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getTargetId()).isEqualTo(secondBlocker.getId());

        harness.passBothPriorities();

        assertThat(firstBlocker.getMarkedDamage()).isZero();
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addReadyJavelineer(Player player) {
        Permanent permanent = new Permanent(new GoblinJavelineer());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
