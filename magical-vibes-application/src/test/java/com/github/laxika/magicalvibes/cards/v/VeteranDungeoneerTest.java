package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeteranDungeoneer.class})
class VeteranDungeoneerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes its controller venture into a dungeon")
    void entersDungeon() {
        harness.castFromHand(player1, new VeteranDungeoneer(), "{3}{W}");
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("Its controller can choose Tomb of Annihilation and resolve the first room")
    void choosesTombOfAnnihilation() {
        harness.castFromHand(player1, new VeteranDungeoneer(), "{3}{W}");
        resolveAllTriggers();
        harness.handleListChoice(player1, "Tomb of Annihilation");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 0));
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An opposing controller chooses their own dungeon and receives its room benefit")
    void opposingControllerChoosesDungeon() {
        harness.castFromHand(player2, new VeteranDungeoneer(), "{3}{W}");
        resolveAllTriggers();
        harness.handleListChoice(player2, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.assertLife(player2, 21);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Entering advances an existing dungeon along the chosen arrow")
    void advancesExistingDungeon() {
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 1));

        harness.castFromHand(player1, new VeteranDungeoneer(), "{3}{W}");
        resolveAllTriggers();
        harness.handleListChoice(player1, "Dark Pool");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 4));
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Venture waits for the enters ability to resolve after the creature spell")
    void ventureWaitsForTriggerResolution() {
        harness.castFromHand(player1, new VeteranDungeoneer(), "{3}{W}");
        assertThat(gd.playerDungeonProgress).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Veteran Dungeoneer");
        assertThat(gd.playerDungeonProgress).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.handleListChoice(player1, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
    }
}
