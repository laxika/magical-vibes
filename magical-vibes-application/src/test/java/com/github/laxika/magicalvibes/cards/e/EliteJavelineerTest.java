package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EliteJavelineer.class, GrizzlyBears.class, Entangler.class})
class EliteJavelineerTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking queues a target selection for an attacking creature")
    void blockingQueuesTargetSelection() {
        addCreatureReady(player2, new EliteJavelineer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Deals 1 damage to the chosen attacking creature")
    void deals1DamageToChosenAttacker() {
        addCreatureReady(player2, new EliteJavelineer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.handlePermanentChosen(player2, attacker.getId());
        harness.passBothPriorities();

        // Attacker (2/2) takes 1 damage but survives
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target an attacking creature it isn't blocking")
    void canTargetUnblockedAttacker() {
        addCreatureReady(player2, new EliteJavelineer());
        Permanent blockedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        blockedAttacker.setAttacking(true);
        otherAttacker.setAttacking(true);

        prepareDeclareBlockers();
        // Javelineer (blocker index 0) blocks the first attacker...
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        // ...but targets the second, unblocked attacker.
        harness.handlePermanentChosen(player2, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(otherAttacker.getMarkedDamage()).isEqualTo(1);
        assertThat(blockedAttacker.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Being blocked does not trigger the ability")
    void beingBlockedDoesNotTriggerAbility() {
        Permanent javelineer = addCreatureReady(player1, new EliteJavelineer());
        javelineer.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature that isn't attacking")
    void cannotTargetNonAttackingCreature() {
        addCreatureReady(player2, new EliteJavelineer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears()); // not attacking

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Blocking multiple creatures triggers only once per combat")
    void blockingMultipleCreaturesTriggersOnlyOncePerCombat() {
        Permanent javelineer = addCreatureReady(player2, new EliteJavelineer());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        harness.setHand(player1, List.of(new Entangler()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0, javelineer.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.pendingInteractions).isEmpty();
    }
}
