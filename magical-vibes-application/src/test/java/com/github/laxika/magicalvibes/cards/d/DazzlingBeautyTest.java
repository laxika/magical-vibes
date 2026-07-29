package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DazzlingBeautyTest extends BaseCardTest {

    private void giveSpell() {
        harness.setHand(player2, List.of(new DazzlingBeauty()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("The targeted unblocked attacker becomes blocked and deals no combat damage")
    void unblockedAttackerBecomesBlockedAndDealsNoDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell();
        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();

        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Without the spell the same unblocked attacker deals its damage")
    void unblockedAttackerNormallyDealsDamage() {
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));

        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A draw is scheduled for the caster at the beginning of the next upkeep")
    void schedulesDrawForCaster() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell();
        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player2.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot be cast outside the declare blockers step")
    void cannotCastOutsideDeclareBlockers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target an already blocked attacking creature")
    void cannotTargetBlockedAttacker() {
        Permanent blockedAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0, 1));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, blockedAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an unblocked attacking creature");
    }

    @Test
    @DisplayName("Cannot target a creature that isn't attacking")
    void cannotTargetNonAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));
        assertThat(attacker.isAttacking()).isTrue();

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an unblocked attacking creature");
    }
}
