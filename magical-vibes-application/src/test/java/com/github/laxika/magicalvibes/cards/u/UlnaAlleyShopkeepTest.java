package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UlnaAlleyShopkeep.class})
class UlnaAlleyShopkeepTest extends BaseCardTest {

    @Test
    @DisplayName("No bonus when you have not gained life this turn")
    void noBonusWithoutLifeGain() {
        Permanent shopkeep = harness.addToBattlefieldAndReturn(player1, new UlnaAlleyShopkeep());

        assertThat(gqs.getEffectivePower(gd, shopkeep)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shopkeep)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +2/+0 while you have gained life this turn")
    void bonusWhileLifeGained() {
        Permanent shopkeep = harness.addToBattlefieldAndReturn(player1, new UlnaAlleyShopkeep());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        assertThat(gqs.getEffectivePower(gd, shopkeep)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shopkeep)).isEqualTo(3);
    }

    @Test
    void oneLifeIsEnoughAndFurtherGainsDoNotIncreaseBonus() {
        Permanent shopkeep = harness.addToBattlefieldAndReturn(player1, new UlnaAlleyShopkeep());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));

        assertThat(gqs.getEffectivePower(gd, shopkeep)).isEqualTo(4);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 5));
        assertThat(gqs.getEffectivePower(gd, shopkeep)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shopkeep)).isEqualTo(3);
    }

    @Test
    void opponentsLifeGainDoesNotEnableBonus() {
        Permanent shopkeep = harness.addToBattlefieldAndReturn(player1, new UlnaAlleyShopkeep());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        assertThat(gqs.getEffectivePower(gd, shopkeep)).isEqualTo(2);
    }

    @Test
    void gainingZeroLifeDoesNotEnableBonus() {
        Permanent shopkeep = harness.addToBattlefieldAndReturn(player1, new UlnaAlleyShopkeep());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0));

        assertThat(gqs.getEffectivePower(gd, shopkeep)).isEqualTo(2);
    }

    @Test
    void lifeLossDoesNotRemoveBonusFromEarlierGain() {
        Permanent shopkeep = harness.addToBattlefieldAndReturn(player1, new UlnaAlleyShopkeep());
        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
            harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 3, "life loss");
        });

        assertThat(gqs.getEffectivePower(gd, shopkeep)).isEqualTo(4);
    }

    @Test
    void lifeGainedBeforeEnteringStillEnablesBonus() {
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        Permanent shopkeep = harness.addToBattlefieldAndReturn(player1, new UlnaAlleyShopkeep());

        assertThat(gqs.getEffectivePower(gd, shopkeep)).isEqualTo(4);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new UlnaAlleyShopkeep());
        addCreatureReady(player2, new UlnaAlleyShopkeep());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new UlnaAlleyShopkeep());
        Permanent firstBlocker = addCreatureReady(player2, new UlnaAlleyShopkeep());
        Permanent secondBlocker = addCreatureReady(player2, new UlnaAlleyShopkeep());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
