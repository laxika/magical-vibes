package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeoninBladetrapTest extends BaseCardTest {

    @Test
    void damagesAttackingCreaturesWithoutFlyingAndSacrificesItself() {
        harness.addToBattlefield(player1, new LeoninBladetrap());

        Permanent attackingGroundCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attackingGroundCreature.setSummoningSick(false);
        attackingGroundCreature.setAttacking(true);

        Permanent attackingFlyingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        attackingFlyingCreature.setSummoningSick(false);
        attackingFlyingCreature.setAttacking(true);

        Permanent nonAttackingGroundCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leonin Bladetrap");
        harness.assertInGraveyard(player1, "Leonin Bladetrap");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attackingGroundCreature);
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonAttackingGroundCreature);
    }
}
