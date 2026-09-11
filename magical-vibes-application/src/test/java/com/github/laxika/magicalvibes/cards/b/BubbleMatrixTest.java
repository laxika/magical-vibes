package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CloudDjinn;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.cards.t.Thunderbolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BubbleMatrix.class, CloudDjinn.class, RedwoodTreefolk.class, Thunderbolt.class})
class BubbleMatrixTest extends BaseCardTest {

    private void giveThunderboltMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private Permanent addAttacker(Player controller) {
        Permanent attacker = addCreatureReady(controller, new RedwoodTreefolk());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBlocker(Player controller, int blockingTarget) {
        Permanent blocker = addCreatureReady(controller, new RedwoodTreefolk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(blockingTarget);
        return blocker;
    }

    @Test
    @DisplayName("Noncombat damage to the controller's creature is prevented")
    void preventsNoncombatDamageToOwnCreature() {
        harness.addToBattlefield(player1, new BubbleMatrix());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CloudDjinn());

        harness.setHand(player2, List.of(new Thunderbolt()));
        giveThunderboltMana(player2);
        harness.castModalInstant(player2, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Noncombat damage to an opponent's creature is prevented too")
    void preventsNoncombatDamageToOpponentCreature() {
        harness.addToBattlefield(player1, new BubbleMatrix());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CloudDjinn());

        harness.setHand(player1, List.of(new Thunderbolt()));
        giveThunderboltMana(player1);
        harness.castModalInstant(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Combat damage between creatures is prevented on both sides")
    void preventsCombatDamage() {
        harness.addToBattlefield(player1, new BubbleMatrix());
        Permanent blocker = addBlocker(player1, 0);
        Permanent attacker = addAttacker(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage to players is not prevented")
    void doesNotPreventDamageToPlayers() {
        harness.addToBattlefield(player1, new BubbleMatrix());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Thunderbolt()));
        giveThunderboltMana(player1);
        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }
}
