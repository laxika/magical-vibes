package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DungeonMap.class)
class DungeonMapTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Dungeon Map adds one colorless mana")
    void tapAddsColorlessMana() {
        Permanent dungeonMap = harness.addToBattlefieldAndReturn(player1, new DungeonMap());

        harness.activateAbility(player1, 0, null, null);

        assertThat(dungeonMap.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating Dungeon Map's second ability makes its controller venture")
    void activatesToVentureIntoDungeon() {
        Permanent dungeonMap = harness.addToBattlefieldAndReturn(player1, new DungeonMap());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(dungeonMap.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Dungeon Map's venture ability cannot be activated outside its controller's main phase")
    void ventureRequiresSorceryTiming() {
        harness.addToBattlefield(player1, new DungeonMap());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }
}
