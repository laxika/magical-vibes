package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MetropolisReformer.class, Shock.class})
class MetropolisReformerTest extends BaseCardTest {

    @Test
    @DisplayName("Controller has hexproof while Metropolis Reformer is on the battlefield")
    void controllerHasHexproof() {
        harness.addToBattlefield(player1, new MetropolisReformer());

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Opponent cannot target the controller")
    void opponentCannotTargetController() {
        harness.addToBattlefield(player1, new MetropolisReformer());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("When Metropolis Reformer is dealt damage, its controller gains that much life")
    void gainsLifeEqualToDamageDealt() {
        Permanent reformer = harness.addToBattlefieldAndReturn(player1, new MetropolisReformer());
        harness.setLife(player1, 10);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, reformer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(reformer.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hexproof permits the controller to target themselves")
    void controllerCanTargetThemselves() {
        harness.addToBattlefield(player1, new MetropolisReformer());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 8);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lethal damage still triggers life gain and removes the controller's hexproof")
    void lethalDamageStillGainsLife() {
        Permanent reformer = harness.addToBattlefieldAndReturn(player1, new MetropolisReformer());
        harness.setLife(player1, 10);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, reformer.getId());
        resolveAllTriggers();
        harness.assertLife(player1, 12);

        harness.castAndResolveInstant(player2, 0, reformer.getId());

        harness.assertNotOnBattlefield(player1, "Metropolis Reformer");
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
        resolveAllTriggers();
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Combat damage gives each Reformer's controller life equal to the damage it receives")
    void combatDamageGainsLifeForBothControllers() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MetropolisReformer());
        attacker.setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new MetropolisReformer());
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 17);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }
}
