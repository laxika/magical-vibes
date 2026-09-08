package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

class TetheredSkirgeTest extends BaseCardTest {

    @Test
    void controllerLosesLifeWhenTargetedBySpell() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new TetheredSkirge());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, skirge.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void controllerLosesLifeWhenTargetedByAbility() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new TetheredSkirge());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player2, 0, null, skirge.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }
}
