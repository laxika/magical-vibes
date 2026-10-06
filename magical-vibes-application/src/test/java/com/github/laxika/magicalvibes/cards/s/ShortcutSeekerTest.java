package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShortcutSeeker.class})
class ShortcutSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Makes its controller venture into a chosen dungeon when it deals combat damage to a player")
    void combatDamageMakesControllerVenture() {
        addCreatureReady(player1, new ShortcutSeeker());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("Does not venture when it deals combat damage only to a blocker")
    void blockedDamageDoesNotVenture() {
        addCreatureReady(player1, new ShortcutSeeker());
        addCreatureReady(player2, new ShortcutSeeker());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The attacking controller chooses the dungeon, not the damaged player")
    void opposingControllerChoosesDungeon() {
        addCreatureReady(player2, new ShortcutSeeker());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();
        harness.handleListChoice(player2, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Combat damage advances an existing dungeon along the chosen path")
    void combatDamageAdvancesExistingDungeon() {
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        addCreatureReady(player1, new ShortcutSeeker());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, "Mine Tunnels");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }
}
