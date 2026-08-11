package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MineLayerTest extends BaseCardTest {

    @Test
    @DisplayName("Activation puts a mine counter on target land")
    void activationPutsMineCounterOnTargetLand() {
        addReadyMineLayer();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.MINE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activation cannot target a creature")
    void activationCannotTargetCreature() {
        addReadyMineLayer();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapping a land with a mine counter destroys it")
    void tappingMinedLandDestroysIt() {
        addReadyMineLayer();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        land.setCounterCount(CounterType.MINE, 1);

        harness.tapPermanent(player2, 0);
        resolveStackFully();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Tapping a land without a mine counter does not destroy it")
    void tappingUnminedLandDoesNotDestroyIt() {
        addReadyMineLayer();
        harness.addToBattlefield(player2, new Mountain());

        harness.tapPermanent(player2, 0);
        resolveStackFully();

        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Leaving the battlefield removes mine counters from all lands")
    void leavingRemovesMineCountersFromAllLands() {
        Permanent mineLayer = addReadyMineLayer();
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent nonland = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownLand.setCounterCount(CounterType.MINE, 2);
        opposingLand.setCounterCount(CounterType.MINE, 1);
        nonland.setCounterCount(CounterType.MINE, 1);

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, mineLayer.getId());
        resolveStackFully();

        assertThat(ownLand.getCounterCount(CounterType.MINE)).isZero();
        assertThat(opposingLand.getCounterCount(CounterType.MINE)).isZero();
        assertThat(nonland.getCounterCount(CounterType.MINE)).isEqualTo(1);
    }

    private Permanent addReadyMineLayer() {
        Permanent mineLayer = harness.addToBattlefieldAndReturn(player1, new MineLayer());
        mineLayer.setSummoningSick(false);
        return mineLayer;
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
