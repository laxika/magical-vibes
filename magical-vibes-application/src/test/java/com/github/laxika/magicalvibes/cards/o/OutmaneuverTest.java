package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutmaneuverTest extends BaseCardTest {

    @Test
    @DisplayName("Targeted blocked creatures deal combat damage to what they attacked")
    void targetedBlockedCreaturesAssignDamageAsThoughUnblocked() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1);
        Permanent blocker = addCreature(player2);
        block(attacker, blocker);

        harness.setHand(player1, List.of(new Outmaneuver()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castInstantForX(player1, 0, 1, List.of(attacker.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Outmaneuver affects exactly the chosen blocked creatures")
    void affectsOnlyChosenBlockedCreatures() {
        harness.setLife(player2, 20);
        Permanent targetedAttacker = addAttacker(player1);
        Permanent untargetedAttacker = addAttacker(player1);
        Permanent targetedBlocker = addCreature(player2);
        Permanent untargetedBlocker = addCreature(player2);
        block(targetedAttacker, targetedBlocker);
        block(untargetedAttacker, untargetedBlocker);

        harness.setHand(player1, List.of(new Outmaneuver()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castInstantForX(player1, 0, 1, List.of(targetedAttacker.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Outmaneuver can target only blocked creatures")
    void canTargetOnlyBlockedCreatures() {
        Permanent attacker = addAttacker(player1);

        harness.setHand(player1, List.of(new Outmaneuver()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Targets must be blocked creatures");
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = addCreature(player);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }

    private Permanent addCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private void block(Permanent attacker, Permanent blocker) {
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));
        blocker.addBlockingTargetId(attacker.getId());
    }
}
