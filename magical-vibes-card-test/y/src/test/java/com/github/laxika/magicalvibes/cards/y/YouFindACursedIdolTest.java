package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.ClericClass;
import com.github.laxika.magicalvibes.cards.t.TreasureChest;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YouFindACursedIdol.class, TreasureChest.class, ClericClass.class})
class YouFindACursedIdolTest extends BaseCardTest {

    @Test
    void smashItDestroysAnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new TreasureChest());

        cast(0, artifact.getId());

        harness.assertNotOnBattlefield(player2, "Treasure Chest");
        harness.assertInGraveyard(player2, "Treasure Chest");
    }

    @Test
    void liftTheCurseDestroysAnEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new ClericClass());

        cast(1, enchantment.getId());

        harness.assertNotOnBattlefield(player2, "Cleric Class");
        harness.assertInGraveyard(player2, "Cleric Class");
    }

    @Test
    void stealItsEyesCreatesTreasureAndVenturesIntoTheDungeon() {
        cast(2);
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    void stealItsEyesAllowsChoosingADifferentDungeonAndResolvesItsRoomAbility() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        cast(2);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.handleListChoice(player1, "Dungeon of the Mad Mage");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        harness.assertLife(player1, lifeBefore + 1);
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void stealItsEyesAdvancesExistingDungeonAlongChosenArrow() {
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));

        cast(2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.handleListChoice(player1, "Mine Tunnels");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void smashItCanDestroyYourOwnArtifactWithoutPerformingOtherModes() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TreasureChest());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new ClericClass());

        cast(0, artifact.getId());

        harness.assertInGraveyard(player1, "Treasure Chest");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchantment);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerDungeonProgress).isEmpty();
    }

    @Test
    void destructionModesOnlyAllowTheirSpecifiedPermanentType() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new ClericClass());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);

        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new TreasureChest());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int modeIndex, java.util.UUID targetId) {
        prepareSpell();
        harness.castModalSorcery(player1, 0, modeIndex, List.of(targetId));
        harness.passBothPriorities();
    }

    private void cast(int modeIndex) {
        prepareSpell();
        harness.castModalSorcery(player1, 0, modeIndex, List.of());
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new YouFindACursedIdol()));
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
