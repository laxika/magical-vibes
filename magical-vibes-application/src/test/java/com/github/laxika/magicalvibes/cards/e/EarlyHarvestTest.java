package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CrystalVein;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarlyHarvest.class, Forest.class, Island.class, CrystalVein.class, FeralShadow.class})
class EarlyHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all basic lands the target player controls")
    void untapsBasicLands() {
        Permanent forest1 = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent forest2 = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        forest1.tap();
        forest2.tap();
        island.tap();

        castEarlyHarvest(player2.getId());

        assertThat(forest1.isTapped()).isFalse();
        assertThat(forest2.isTapped()).isFalse();
        assertThat(island.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap non-basic lands")
    void doesNotUntapNonBasicLands() {
        Permanent nonBasic = harness.addToBattlefieldAndReturn(player2, new CrystalVein());
        nonBasic.tap();

        castEarlyHarvest(player2.getId());

        assertThat(nonBasic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap creatures")
    void doesNotUntapCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FeralShadow());
        creature.tap();

        castEarlyHarvest(player2.getId());

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target yourself, untapping your own basic lands")
    void canTargetYourself() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        castEarlyHarvest(player1.getId());

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only untaps the target player's lands, not another player's")
    void onlyAffectsTargetPlayer() {
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent targetForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        ownForest.tap();
        targetForest.tap();

        castEarlyHarvest(player2.getId());

        assertThat(targetForest.isTapped()).isFalse();
        assertThat(ownForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target a player who controls no basic lands")
    void canTargetPlayerWithNoBasicLands() {
        castEarlyHarvest(player2.getId());

        assertThat(gd.stack).isEmpty();
    }

    private void castEarlyHarvest(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new EarlyHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
