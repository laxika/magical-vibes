package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NadaarSelflessPaladin.class, HillGiantHerdgorger.class})
class NadaarSelflessPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes its controller venture into a dungeon")
    void entersDungeon() {
        harness.castFromHand(player1, new NadaarSelflessPaladin(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("Attacking with Nadaar makes its controller venture into a dungeon")
    void attackingVentureIntoDungeon() {
        Permanent nadaar = addCreatureReady(player1, new NadaarSelflessPaladin());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(nadaar.isTapped()).isFalse();
    }

    @Test
    @DisplayName("After completing a dungeon, other creatures you control get +1/+1")
    void buffsOtherCreaturesAfterDungeonCompletion() {
        Permanent nadaar = harness.addToBattlefieldAndReturn(player1, new NadaarSelflessPaladin());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        int nadaarPowerBefore = gqs.getEffectivePower(gd, nadaar);
        int nadaarToughnessBefore = gqs.getEffectiveToughness(gd, nadaar);
        int giantPowerBefore = gqs.getEffectivePower(gd, giant);
        int giantToughnessBefore = gqs.getEffectiveToughness(gd, giant);

        gd.playersWhoCompletedDungeon.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, nadaar)).isEqualTo(nadaarPowerBefore);
        assertThat(gqs.getEffectiveToughness(gd, nadaar)).isEqualTo(nadaarToughnessBefore);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(giantPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(giantToughnessBefore + 1);
    }

    @Test
    @DisplayName("An opponent's completed dungeon does not enable Nadaar's bonus")
    void opponentsCompletionDoesNotEnableBonus() {
        harness.addToBattlefield(player1, new NadaarSelflessPaladin());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        int powerBefore = gqs.getEffectivePower(gd, giant);
        int toughnessBefore = gqs.getEffectiveToughness(gd, giant);

        gd.playersWhoCompletedDungeon.add(player2.getId());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("A dungeon completed before Nadaar entered enables only its controller's creatures")
    void previousCompletionEnablesBonusOnlyForController() {
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        Permanent opposingGiant = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        int ownPowerBefore = gqs.getEffectivePower(gd, ownGiant);
        int ownToughnessBefore = gqs.getEffectiveToughness(gd, ownGiant);
        int opposingPowerBefore = gqs.getEffectivePower(gd, opposingGiant);
        int opposingToughnessBefore = gqs.getEffectiveToughness(gd, opposingGiant);
        gd.playersWhoCompletedDungeon.add(player1.getId());

        Permanent nadaar = harness.addToBattlefieldAndReturn(player1, new NadaarSelflessPaladin());

        assertThat(gqs.getEffectivePower(gd, ownGiant)).isEqualTo(ownPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownGiant)).isEqualTo(ownToughnessBefore + 1);
        assertThat(gqs.getEffectivePower(gd, opposingGiant)).isEqualTo(opposingPowerBefore);
        assertThat(gqs.getEffectiveToughness(gd, opposingGiant)).isEqualTo(opposingToughnessBefore);

        gd.playerBattlefields.get(player1.getId()).remove(nadaar);

        assertThat(gqs.getEffectivePower(gd, ownGiant)).isEqualTo(ownPowerBefore);
        assertThat(gqs.getEffectiveToughness(gd, ownGiant)).isEqualTo(ownToughnessBefore);
    }

    @Test
    @DisplayName("The bonus starts after the final dungeon room ability resolves")
    void finalRoomResolutionEnablesBonus() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        int powerBefore = gqs.getEffectivePower(gd, giant);
        int toughnessBefore = gqs.getEffectiveToughness(gd, giant);
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 4));
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger()));

        harness.castFromHand(player1, new NadaarSelflessPaladin(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 6));
        assertThat(gd.playersWhoCompletedDungeon).doesNotContain(player1.getId());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(toughnessBefore);

        harness.passBothPriorities();

        assertThat(gd.playersWhoCompletedDungeon).contains(player1.getId());
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("An opposing Nadaar's attack ventures for that player and resolves its chosen room")
    void opposingControllerChoosesDungeonAndGetsRoomEffect() {
        addCreatureReady(player2, new NadaarSelflessPaladin());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Dungeon of the Mad Mage");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.assertLife(player2, lifeBefore + 1);
    }
}
