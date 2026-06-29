package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PegasusCourserTest extends BaseCardTest {

    // ===== Attack trigger: grant flying =====

    @Test
    @DisplayName("Attacking with Pegasus Courser queues target selection for another attacking creature")
    void attackTriggerQueuesForTargetSelection() {
        Permanent courser = addCourserReady(player1);
        Permanent bears = addCreatureReady(player1);

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Targeted attacking creature gains flying until end of turn")
    void targetedCreatureGainsFlying() {
        Permanent courser = addCourserReady(player1);
        Permanent bears = addCreatureReady(player1);

        declareAttackers(player1, List.of(0, 1));

        // Choose bears as target
        harness.handlePermanentChosen(player1, bears.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target Pegasus Courser itself (another restriction)")
    void cannotTargetItself() {
        Permanent courser = addCourserReady(player1);
        Permanent bears = addCreatureReady(player1);

        declareAttackers(player1, List.of(0, 1));

        // Choosing Pegasus Courser itself should fail
        assertThatThrownBy(
                () -> harness.handlePermanentChosen(player1, courser.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No trigger when attacking alone (no valid targets)")
    void noTriggerWhenAttackingAlone() {
        Permanent courser = addCourserReady(player1);

        declareAttackers(player1, List.of(0));

        // Should not be awaiting permanent choice since there are no other attacking creatures
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        Permanent courser = addCourserReady(player1);
        Permanent attacker = addCreatureReady(player1);
        Permanent stayBack = addCreatureReady(player1);

        // Only courser (index 0) and attacker (index 1) attack; stayBack (index 2) stays back
        declareAttackers(player1, List.of(0, 1));

        // Choosing the non-attacking creature should fail
        assertThatThrownBy(
                () -> harness.handlePermanentChosen(player1, stayBack.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attack trigger puts triggered ability on the stack")
    void attackPutsTriggeredAbilityOnStack() {
        Permanent courser = addCourserReady(player1);
        Permanent bears = addCreatureReady(player1);

        declareAttackers(player1, List.of(0, 1));

        // Choose bears as target
        harness.handlePermanentChosen(player1, bears.getId());

        // Triggered ability should be on the stack
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.stream()
                .anyMatch(entry -> entry.getCard().getName().equals("Pegasus Courser")))
                .isTrue();
    }

    // ===== Helpers =====

    private Permanent addCourserReady(Player player) {
        Permanent perm = new Permanent(new PegasusCourser());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
