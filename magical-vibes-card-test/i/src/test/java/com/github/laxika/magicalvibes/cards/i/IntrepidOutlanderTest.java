package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.cards.y.YouComeToARiver;
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

@CardUsed({IntrepidOutlander.class, DireWolfProwler.class, YouComeToARiver.class})
class IntrepidOutlanderTest extends BaseCardTest {

    @Test
    @DisplayName("Pack tactics makes its controller venture at total attacking power six")
    void packTacticsVenturesAtThreshold() {
        addCreatureReady(player1, new IntrepidOutlander());
        addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new DireWolfProwler());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Pack tactics does not trigger below total attacking power six")
    void packTacticsDoesNotVentureBelowThreshold() {
        Permanent outlander = addCreatureReady(player1, new IntrepidOutlander());
        addCreatureReady(player1, new DireWolfProwler());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(outlander), 1));
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Pack tactics requires Intrepid Outlander to attack")
    void packTacticsRequiresSourceToAttack() {
        addCreatureReady(player1, new IntrepidOutlander());
        addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new DireWolfProwler());

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    @Test
    void packTacticsStillVenturesAfterAnotherAttackerLeaves() {
        addCreatureReady(player1, new IntrepidOutlander());
        Permanent support = addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new DireWolfProwler());
        declareAttackers(List.of(0, 1, 2));

        harness.setHand(player2, List.of(new YouComeToARiver()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, 0, support.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Dire Wolf Prowler");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "Tomb of Annihilation");
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    void packTacticsStillVenturesAfterOutlanderLeaves() {
        Permanent outlander = addCreatureReady(player1, new IntrepidOutlander());
        addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new DireWolfProwler());
        declareAttackers(List.of(0, 1, 2));

        harness.setHand(player2, List.of(new YouComeToARiver()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, 0, outlander.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Intrepid Outlander");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    void increasingPowerAfterAttackDoesNotCreatePackTacticsTrigger() {
        addCreatureReady(player1, new IntrepidOutlander());
        Permanent prowler = addCreatureReady(player1, new DireWolfProwler());
        declareAttackers(List.of(0, 1));
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(prowler), null, null);
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void packTacticsAdvancesAnExistingDungeon() {
        gd.playerDungeonProgress.put(player1.getId(), new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 1));
        addCreatureReady(player1, new IntrepidOutlander());
        addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new DireWolfProwler());

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 2));
    }
}
