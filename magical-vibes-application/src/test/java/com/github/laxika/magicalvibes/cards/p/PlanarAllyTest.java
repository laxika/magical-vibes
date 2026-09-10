package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PlanarAlly.class)
class PlanarAllyTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Planar Ally makes its controller venture into a dungeon")
    void attackingVentureIntoDungeon() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new PlanarAlly());
        ally.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }
}
