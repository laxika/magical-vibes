package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoggartLoggers.class, BlackPoplarShaman.class, GoldmeadowHarrier.class, Forest.class, Island.class})
class BoggartLoggersTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing to destroy a target Treefolk removes both permanents")
    void destroysTargetTreefolk() {
        Permanent loggers = addCreatureReady(player1, new BoggartLoggers());
        Permanent treefolk = addCreatureReady(player2, new BlackPoplarShaman());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, treefolk.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(loggers);
        harness.assertInGraveyard(player1, "Boggart Loggers");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(treefolk);
        harness.assertInGraveyard(player2, "Black Poplar Shaman");
    }

    @Test
    @DisplayName("Can destroy a target Forest")
    void destroysTargetForest() {
        addCreatureReady(player1, new BoggartLoggers());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(forest);
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Forestwalk prevents blocking while the defending player controls a Forest")
    void forestwalkPreventsBlockingWhenDefenderControlsForest() {
        Permanent loggers = addCreatureReady(player1, new BoggartLoggers());
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new GoldmeadowHarrier());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> declareBlock(blocker, loggers))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Forestwalk allows blocking while the defending player controls no Forest")
    void forestwalkAllowsBlockingWithoutDefendingForest() {
        Permanent loggers = addCreatureReady(player1, new BoggartLoggers());
        Permanent blocker = addCreatureReady(player2, new GoldmeadowHarrier());

        declareAttackersAndPrepareBlockers(List.of(0));
        declareBlock(blocker, loggers);

        assertThat(blocker.getBlockingTargetIds()).containsExactly(loggers.getId());
    }

    @Test
    @DisplayName("Cannot target a non-Treefolk creature")
    void cannotTargetNonTreefolkCreature() {
        addCreatureReady(player1, new BoggartLoggers());
        Permanent harrier = addCreatureReady(player2, new GoldmeadowHarrier());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, harrier.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Forest land")
    void cannotTargetNonForestLand() {
        addCreatureReady(player1, new BoggartLoggers());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new BoggartLoggers());
        Permanent treefolk = addCreatureReady(player2, new BlackPoplarShaman());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, treefolk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, and a tapped summoning-sick Loggers can activate")
    void paysSacrificeBeforeResolutionWithoutTapRestrictions() {
        Permanent loggers = harness.addToBattlefieldAndReturn(player1, new BoggartLoggers());
        loggers.setSummoningSick(true);
        loggers.tap();
        Permanent treefolk = addCreatureReady(player2, new BlackPoplarShaman());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, treefolk.getId());

        harness.assertInGraveyard(player1, "Boggart Loggers");
        harness.assertNotOnBattlefield(player1, "Boggart Loggers");
        harness.assertOnBattlefield(player2, "Black Poplar Shaman");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Black Poplar Shaman");
        harness.assertNotOnBattlefield(player2, "Black Poplar Shaman");
    }

    @Test
    @DisplayName("Can destroy its controller's Forest")
    void destroysOwnForest() {
        addCreatureReady(player1, new BoggartLoggers());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The attacking player's Forest does not prevent blocking")
    void forestwalkDoesNotCheckAttackersForest() {
        Permanent loggers = addCreatureReady(player1, new BoggartLoggers());
        harness.addToBattlefield(player1, new Forest());
        Permanent blocker = addCreatureReady(player2, new GoldmeadowHarrier());

        declareAttackersAndPrepareBlockers(List.of(0));
        declareBlock(blocker, loggers);

        assertThat(blocker.getBlockingTargetIds()).containsExactly(loggers.getId());
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
