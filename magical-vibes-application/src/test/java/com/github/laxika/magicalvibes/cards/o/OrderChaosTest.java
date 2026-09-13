package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrderChaos.class, GrizzlyBears.class})
class OrderChaosTest extends BaseCardTest {

    @Test
    void orderExilesTargetAttackingCreature() {
        Permanent attacker = addAttacker(player2);
        harness.setHand(player1, List.of(new OrderChaos()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.exiledCards)
                .anyMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));
    }

    @Test
    void orderCannotTargetNonAttackingCreature() {
        addAttacker(player2);
        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OrderChaos()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(nonAttacker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chaosMakesAllCreaturesUnableToBlockThisTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OrderChaos()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, ownCreature, opposingCreature,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, opposingCreature, ownCreature,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void chaosRestrictionExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OrderChaos()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();
        Permanent attackerForPlayer1 = addCreatureReady(player1, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, creature, attackerForPlayer1,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, creature, attackerForPlayer1,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(owner.getId()).add(attacker);
        return attacker;
    }
}
