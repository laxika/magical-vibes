package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BurstOfEnergy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TetheredSkirge.class, BurstOfEnergy.class, ThornwindFaeries.class})
class TetheredSkirgeTest extends BaseCardTest {

    @Test
    void controllerLosesLifeWhenTargetedBySpell() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new TetheredSkirge());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BurstOfEnergy()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, skirge.getId());

        harness.assertLife(player1, 19);
    }

    @Test
    void controllerLosesLifeWhenTargetedByOwnSpell() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new TetheredSkirge());

        harness.setHand(player1, List.of(new BurstOfEnergy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, skirge.getId());

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

    @Test
    void lifeLossUsesTheStackAndResolvesBeforeTheTargetingSpell() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new TetheredSkirge());
        skirge.tap();
        harness.setHand(player1, List.of(new BurstOfEnergy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, skirge.getId());

        harness.assertLife(player1, 20);
        assertThat(skirge.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(skirge.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(skirge.isTapped()).isFalse();
    }

    @Test
    void eachSpellTargetingTheSameSkirgeTriggersLifeLoss() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new TetheredSkirge());
        harness.setHand(player1, List.of(new BurstOfEnergy(), new BurstOfEnergy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, skirge.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);

        harness.castAndResolveInstant(player1, 0, skirge.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void targetingAnotherCreatureDoesNotTriggerSkirge() {
        harness.addToBattlefield(player1, new TetheredSkirge());
        Permanent faeries = harness.addToBattlefieldAndReturn(player1, new ThornwindFaeries());
        harness.setHand(player1, List.of(new BurstOfEnergy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, faeries.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
