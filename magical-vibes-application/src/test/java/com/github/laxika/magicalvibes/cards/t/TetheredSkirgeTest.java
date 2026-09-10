package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BurstOfEnergy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({TetheredSkirge.class, BurstOfEnergy.class, ThornwindFaeries.class})
class TetheredSkirgeTest extends BaseCardTest {

    @Test
    void controllerLosesLifeWhenTargetedBySpell() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new TetheredSkirge());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BurstOfEnergy()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, skirge.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void controllerLosesLifeWhenTargetedByOwnSpell() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new TetheredSkirge());

        harness.setHand(player1, List.of(new BurstOfEnergy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, skirge.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void controllerLosesLifeWhenTargetedByAbility() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new TetheredSkirge());
        addCreatureReady(player2, new ThornwindFaeries());

        harness.activateAbility(player2, 0, null, skirge.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }
}
