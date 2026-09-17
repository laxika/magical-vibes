package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.Repel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BombSquad.class, DwarvenGrunt.class, Forest.class, Repel.class})
class BombSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability puts a fuse counter on target creature")
    void tapAbilityPutsFuseCounterOnTargetCreature() {
        Permanent bombSquad = addReadyBombSquad(player1);
        Permanent grunt = harness.addToBattlefieldAndReturn(player2, new DwarvenGrunt());

        harness.activateAbility(player1, 0, null, grunt.getId());
        harness.passBothPriorities();

        assertThat(bombSquad.getCounterCount(CounterType.FUSE)).isZero();
        assertThat(grunt.getCounterCount(CounterType.FUSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep ability puts a fuse counter on every creature that already has one")
    void upkeepAbilityAddsFuseCounterToFusedCreatures() {
        addReadyBombSquad(player1);
        Permanent opponentGrunt = harness.addToBattlefieldAndReturn(player2, new DwarvenGrunt());
        Permanent controllerGrunt = harness.addToBattlefieldAndReturn(player1, new DwarvenGrunt());
        Permanent unfusedGrunt = harness.addToBattlefieldAndReturn(player2, new DwarvenGrunt());
        opponentGrunt.setCounterCount(CounterType.FUSE, 1);
        controllerGrunt.setCounterCount(CounterType.FUSE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(opponentGrunt.getCounterCount(CounterType.FUSE)).isEqualTo(2);
        assertThat(controllerGrunt.getCounterCount(CounterType.FUSE)).isEqualTo(2);
        assertThat(unfusedGrunt.getCounterCount(CounterType.FUSE)).isZero();
    }

    @Test
    @DisplayName("Four fuse counters detonate the creature and damage its controller")
    void fourFuseCountersDestroyCreatureAndDamageController() {
        addReadyBombSquad(player1);
        Permanent grunt = harness.addToBattlefieldAndReturn(player2, new DwarvenGrunt());
        grunt.setCounterCount(CounterType.FUSE, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, grunt.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dwarven Grunt");
        harness.assertInGraveyard(player2, "Dwarven Grunt");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("More than four fuse counters also detonate the creature")
    void moreThanFourFuseCountersAlsoDetonateTheCreature() {
        addReadyBombSquad(player1);
        Permanent grunt = harness.addToBattlefieldAndReturn(player2, new DwarvenGrunt());
        grunt.setCounterCount(CounterType.FUSE, 5);
        harness.setLife(player2, 20);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dwarven Grunt");
        harness.assertInGraveyard(player2, "Dwarven Grunt");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Separate Bomb Squad triggers each damage the creature's controller")
    void separateTriggersEachDealDamageAfterTheFirstDestroysCreature() {
        addReadyBombSquad(player1);
        addReadyBombSquad(player1);
        Permanent grunt = harness.addToBattlefieldAndReturn(player2, new DwarvenGrunt());
        grunt.setCounterCount(CounterType.FUSE, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, grunt.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Dwarven Grunt");
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("The fuse trigger still damages the controller if the creature leaves before resolution")
    void fuseTriggerStillDamagesControllerIfCreatureLeavesBeforeResolution() {
        addReadyBombSquad(player1);
        Permanent grunt = harness.addToBattlefieldAndReturn(player2, new DwarvenGrunt());
        grunt.setCounterCount(CounterType.FUSE, 4);
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new Repel()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.runStateBasedActions();

        harness.passPriority(player1);
        harness.castInstant(player2, 0, grunt.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Dwarven Grunt");
        assertThat(gameData.playerDecks.get(player2.getId())).first().isSameAs(grunt.getCard());
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Regeneration does not prevent fuse removal or damage")
    void regenerationDoesNotPreventFuseRemovalOrDamage() {
        addReadyBombSquad(player1);
        Permanent grunt = harness.addToBattlefieldAndReturn(player2, new DwarvenGrunt());
        grunt.setCounterCount(CounterType.FUSE, 3);
        grunt.setRegenerationShield(1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, grunt.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dwarven Grunt");
        assertThat(grunt.getCounterCount(CounterType.FUSE)).isZero();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Tap ability cannot target a land")
    void tapAbilityCannotTargetLand() {
        addReadyBombSquad(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBombSquad(Player player) {
        return addCreatureReady(player, new BombSquad());
    }
}
