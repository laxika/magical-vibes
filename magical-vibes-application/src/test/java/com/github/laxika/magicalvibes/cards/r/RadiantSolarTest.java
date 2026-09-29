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

@CardUsed({RadiantSolar.class, JunglebornPioneer.class})
class RadiantSolarTest extends BaseCardTest {

    @Test
    @DisplayName("Ventures when it enters the battlefield")
    void entersAndVentures() {
        harness.setHand(player1, List.of(new RadiantSolar()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Ventures for another nontoken creature but not its token")
    void anotherNontokenCreatureTriggersOnlyOnce() {
        harness.addToBattlefield(player1, new RadiantSolar());
        harness.setHand(player1, List.of(new JunglebornPioneer()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("The hand ability ventures and gains life")
    void handAbilityVenturesAndGainsLife() {
        harness.setHand(player1, List.of(new RadiantSolar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 10);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }
}
