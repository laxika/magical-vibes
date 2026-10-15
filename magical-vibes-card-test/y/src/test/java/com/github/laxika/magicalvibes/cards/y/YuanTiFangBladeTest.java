package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YuanTiFangBlade.class, HillGiantHerdgorger.class})
class YuanTiFangBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Ventures into a chosen dungeon when it deals combat damage to a player")
    void combatDamageMakesControllerVenture() {
        addCreatureReady(player1, new YuanTiFangBlade());
        harness.setLibrary(player1, List.of());

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
    @DisplayName("Does not venture when blocked, and deathtouch kills a larger blocker")
    void blockedDamageDoesNotVenture() {
        addCreatureReady(player1, new YuanTiFangBlade());
        addCreatureReady(player2, new HillGiantHerdgorger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.assertInGraveyard(player2, "Hill Giant Herdgorger");
        harness.assertInGraveyard(player1, "Yuan-Ti Fang-Blade");
    }

    @Test
    void combatDamageAdvancesThroughChosenBranchOfExistingDungeon() {
        addCreatureReady(player1, new YuanTiFangBlade());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, "Mine Tunnels");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        harness.assertOnBattlefield(player1, "Treasure");
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    void otherPlayersFangBladeVenturesForItsController() {
        addCreatureReady(player2, new YuanTiFangBlade());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();
        harness.handleListChoice(player2, "Tomb of Annihilation");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }
}
