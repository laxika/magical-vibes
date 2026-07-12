package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EliteJavelineerTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking queues a target selection for an attacking creature")
    void blockingQueuesTargetSelection() {
        addReadyJavelineer(player2);
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Deals 1 damage to the chosen attacking creature")
    void deals1DamageToChosenAttacker() {
        addReadyJavelineer(player2);
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.handlePermanentChosen(player2, attacker.getId());
        harness.passBothPriorities();

        // Attacker (2/2) takes 1 damage but survives
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target an attacking creature it isn't blocking")
    void canTargetUnblockedAttacker() {
        addReadyJavelineer(player2);
        Permanent blockedAttacker = addReadyBears(player1);
        Permanent otherAttacker = addReadyBears(player1);
        blockedAttacker.setAttacking(true);
        otherAttacker.setAttacking(true);

        setupDeclareBlockers();
        // Javelineer (blocker index 0) blocks the first attacker...
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        // ...but targets the second, unblocked attacker.
        harness.handlePermanentChosen(player2, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(otherAttacker.getMarkedDamage()).isEqualTo(1);
        assertThat(blockedAttacker.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a creature that isn't attacking")
    void cannotTargetNonAttackingCreature() {
        addReadyJavelineer(player2);
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);
        Permanent bystander = addReadyBears(player2); // not attacking

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    // ===== Helpers =====

    private Permanent addReadyJavelineer(Player player) {
        Permanent perm = new Permanent(new EliteJavelineer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
