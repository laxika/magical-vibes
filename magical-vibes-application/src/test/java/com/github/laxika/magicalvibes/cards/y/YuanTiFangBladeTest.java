package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YuanTiFangBlade.class, GrizzlyBears.class})
class YuanTiFangBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Ventures into a dungeon when it deals combat damage to a player")
    void combatDamageMakesControllerVenture() {
        addCreatureReady(player1, new YuanTiFangBlade());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Does not venture when blocked")
    void blockedDamageDoesNotVenture() {
        addCreatureReady(player1, new YuanTiFangBlade());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }
}
