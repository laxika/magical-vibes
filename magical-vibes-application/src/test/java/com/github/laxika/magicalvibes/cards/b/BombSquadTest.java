package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BombSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability puts a fuse counter on target creature")
    void tapAbilityPutsFuseCounterOnTargetCreature() {
        Permanent bombSquad = addReadyBombSquad(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bombSquad.getCounterCount(CounterType.FUSE)).isZero();
        assertThat(bears.getCounterCount(CounterType.FUSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep ability puts a fuse counter on every creature that already has one")
    void upkeepAbilityAddsFuseCounterToFusedCreatures() {
        addReadyBombSquad(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.FUSE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.FUSE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Four fuse counters detonate the creature and damage its controller")
    void fourFuseCountersDestroyCreatureAndDamageController() {
        addReadyBombSquad(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.FUSE, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Separate Bomb Squad triggers each damage the creature's controller")
    void separateTriggersEachDealDamageAfterTheFirstDestroysCreature() {
        addReadyBombSquad(player1);
        addReadyBombSquad(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.FUSE, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Regeneration does not prevent fuse removal or damage")
    void regenerationDoesNotPreventFuseRemovalOrDamage() {
        addReadyBombSquad(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.FUSE, 3);
        bears.setRegenerationShield(1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.FUSE)).isZero();
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
        Permanent permanent = new Permanent(new BombSquad());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
