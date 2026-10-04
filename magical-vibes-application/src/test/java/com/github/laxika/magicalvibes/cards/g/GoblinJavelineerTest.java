package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.cards.b.BattleCryGoblin;
import com.github.laxika.magicalvibes.cards.h.HobgoblinCaptain;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinJavelineer.class, BattleCryGoblin.class, HobgoblinCaptain.class})
class GoblinJavelineerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked queues the blocking creature for target selection")
    void becomingBlockedQueuesTargetSelection() {
        Permanent javelineer = addCreatureReady(player1, new GoblinJavelineer());
        javelineer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BattleCryGoblin());

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
        Permanent javelineer = addCreatureReady(player1, new GoblinJavelineer());
        javelineer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BattleCryGoblin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.handlePermanentChosen(player1, blocker.getId());

        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Battle Cry Goblin");
    }

    @Test
    @DisplayName("Multiple blockers create one trigger and only the chosen blocker is damaged")
    void multipleBlockersCreateOneTargetedTrigger() {
        Permanent javelineer = addCreatureReady(player1, new GoblinJavelineer());
        javelineer.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new BattleCryGoblin());
        Permanent secondBlocker = addCreatureReady(player2, new BattleCryGoblin());

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

    @Test
    @DisplayName("Only creatures blocking this Javelineer are legal targets")
    void excludesCreaturesBlockingAnotherAttackerAndNonblockers() {
        Permanent javelineer = addCreatureReady(player1, new GoblinJavelineer());
        javelineer.setAttacking(true);
        Permanent otherAttacker = addCreatureReady(player1, new BattleCryGoblin());
        otherAttacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BattleCryGoblin());
        Permanent otherBlocker = addCreatureReady(player2, new BattleCryGoblin());
        Permanent nonblocker = addCreatureReady(player2, new BattleCryGoblin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(blocker.getId())
                .doesNotContain(otherBlocker.getId(), nonblocker.getId());
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(otherBlocker.getMarkedDamage()).isZero();
        assertThat(nonblocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The trigger kills a one-toughness blocker before combat damage")
    void killsOneToughnessBlockerBeforeCombatDamage() {
        Permanent javelineer = addCreatureReady(player1, new GoblinJavelineer());
        javelineer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HobgoblinCaptain());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hobgoblin Captain");
        harness.assertNotOnBattlefield(player2, "Hobgoblin Captain");
        harness.assertOnBattlefield(player1, "Goblin Javelineer");
        assertThat(javelineer.getMarkedDamage()).isZero();
    }
}
