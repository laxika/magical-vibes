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
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

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

    @Test
    void canChooseTombOfAnnihilation() {
        harness.addToBattlefield(player1, new DungeonMap());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Tomb of Annihilation");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 0));
        resolveAllTriggers();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    void canChooseDungeonOfTheMadMage() {
        harness.addToBattlefield(player1, new DungeonMap());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    void advancesExistingDungeonAlongChosenBranch() {
        harness.addToBattlefield(player1, new DungeonMap());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.playerDungeonProgress.put(player1.getId(), new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Mine Tunnels");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        harness.assertOnBattlefield(player1, "Treasure");
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    void ventureCannotBeActivatedOnOpponentsTurn() {
        Permanent dungeonMap = harness.addToBattlefieldAndReturn(player1, new DungeonMap());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dungeonMap.isTapped()).isFalse();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    @Test
    void ventureCannotBeActivatedWithNonemptyStack() {
        harness.addToBattlefield(player1, new DungeonMap());
        Permanent secondMap = harness.addToBattlefieldAndReturn(player1, new DungeonMap());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(secondMap.isTapped()).isFalse();
    }

    @Test
    void ventureRequiresThreeMana() {
        Permanent dungeonMap = harness.addToBattlefieldAndReturn(player1, new DungeonMap());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dungeonMap.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityResolvesImmediatelyOutsideMainPhase() {
        Permanent dungeonMap = harness.addToBattlefieldAndReturn(player1, new DungeonMap());
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, null);

        assertThat(dungeonMap.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }
}
