package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClatteringSkeletons.class, PowerWordKill.class})
class ClatteringSkeletonsTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, its controller chooses a dungeon and enters its first room")
    void diesAndVenturesIntoDungeon() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new ClatteringSkeletons());
        harness.setHand(player1, List.of(new PowerWordKill()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, skeletons.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Clattering Skeletons");
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("The controller may choose a different dungeon and its room ability resolves")
    void canChooseMadMage() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new ClatteringSkeletons());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PowerWordKill()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, skeletons.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dungeon of the Mad Mage");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Dying advances an existing dungeon through the chosen outgoing room")
    void advancesExistingDungeon() {
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new ClatteringSkeletons());
        harness.setHand(player1, List.of(new PowerWordKill()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, skeletons.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Mine Tunnels");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        harness.assertOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("An opponent's Skeletons ventures for that opponent, not the spell's controller")
    void opponentControlsDeathTrigger() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player2, new ClatteringSkeletons());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PowerWordKill()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, skeletons.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Dungeon of the Mad Mage");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Entering the battlefield does not venture")
    void enteringDoesNotVenture() {
        harness.enterBattlefieldAndReturn(player1, new ClatteringSkeletons());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDungeonProgress).isEmpty();
    }
}
