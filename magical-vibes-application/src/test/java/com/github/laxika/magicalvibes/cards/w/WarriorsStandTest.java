package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarriorsStand.class, WuInfantry.class, Plains.class})
class WarriorsStandTest extends BaseCardTest {

    @Test
    @DisplayName("Cast during declare attackers while attacked: creatures you control get +2/+2")
    void boostsOwnCreaturesWhenAttacked() {
        Permanent first = addCreatureReady(player2, new WuInfantry());
        Permanent second = addCreatureReady(player2, new WuInfantry());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player2, new WarriorsStand(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(land.getPowerModifier()).isZero();
        assertThat(land.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Does not boost the opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent opponentCreature = addAttacker(player1, player2);
        Permanent ownCreature = addCreatureReady(player2, new WuInfantry());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player2, new WarriorsStand(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot cast during declare attackers if not attacked")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castFromHand(player2, new WarriorsStand(), "{1}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castFromHand(player2, new WarriorsStand(), "{1}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent creature = addCreatureReady(player2, new WuInfantry());
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player2, new WarriorsStand(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    private Permanent addAttacker(Player attackerController, Player defender) {
        Permanent perm = addCreatureReady(attackerController, new WuInfantry());
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}
