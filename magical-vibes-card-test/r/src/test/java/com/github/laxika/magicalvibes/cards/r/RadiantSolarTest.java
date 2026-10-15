package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.j.JunglebornPioneer;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RadiantSolar.class, JunglebornPioneer.class})
class RadiantSolarTest extends BaseCardTest {

    @Test
    @DisplayName("Ventures when it enters the battlefield")
    void entersAndVentures() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new RadiantSolar()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Ventures for another nontoken creature but not its token")
    void anotherNontokenCreatureTriggersOnlyOnce() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new RadiantSolar());
        harness.setHand(player1, List.of(new JunglebornPioneer()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("The hand ability ventures and gains life")
    void handAbilityVenturesAndGainsLife() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new RadiantSolar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 10);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Radiant Solar");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 10);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    void opponentCreatureDoesNotTriggerSolar() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new RadiantSolar());
        harness.setHand(player2, List.of(new RadiantSolar()));
        harness.addMana(player2, ManaColor.WHITE, 6);

        harness.castCreature(player2, 0);
        resolveAllTriggers();
        harness.handleListChoice(player2, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        harness.assertLife(player2, 21);
    }

    @Test
    void handAbilityAdvancesExistingDungeon() {
        harness.setHand(player1, List.of(new RadiantSolar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 10);
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Mine Tunnels");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        harness.assertOnBattlefield(player1, "Treasure");
        harness.assertLife(player1, 13);
    }

    @Test
    void handAbilityRequiresWhiteManaBeforeDiscarding() {
        RadiantSolar solar = new RadiantSolar();
        harness.setHand(player1, List.of(solar));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(solar);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }
}
