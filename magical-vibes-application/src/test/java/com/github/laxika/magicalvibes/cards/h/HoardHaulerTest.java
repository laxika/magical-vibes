package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoardHauler.class, LeoninScimitar.class, Ornithopter.class, GrizzlyBears.class})
class HoardHaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure for each artifact controlled by the damaged player")
    void createsTreasureForEachArtifactControlledByDamagedPlayer() {
        Permanent hauler = addCreatureReady(player1, new HoardHauler());
        hauler.setAnimatedUntilEndOfTurn(true);
        hauler.setAnimatedPower(5);
        hauler.setAnimatedToughness(5);
        hauler.setAttacking(true);

        addCreatureReady(player2, new LeoninScimitar());
        addCreatureReady(player2, new Ornithopter());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Creates Treasures when trample deals damage through a blocker")
    void createsTreasureWhenTramplingOverBlocker() {
        Permanent hauler = addCreatureReady(player1, new HoardHauler());
        hauler.setAnimatedUntilEndOfTurn(true);
        hauler.setAnimatedPower(5);
        hauler.setAnimatedToughness(5);
        hauler.setAttacking(true);

        addCreatureReady(player2, new LeoninScimitar());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void createsNoTreasureWhenBlockerAbsorbsAllCombatDamage() {
        Permanent hauler = addCreatureReady(player1, new HoardHauler());
        hauler.setAnimatedUntilEndOfTurn(true);
        hauler.setAnimatedPower(5);
        hauler.setAnimatedToughness(5);
        hauler.setAttacking(true);
        addCreatureReady(player2, new LeoninScimitar());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void createsNoTreasureWhenDamagedPlayerControlsNoArtifacts() {
        Permanent hauler = addCreatureReady(player1, new HoardHauler());
        hauler.setAnimatedUntilEndOfTurn(true);
        hauler.setAnimatedPower(5);
        hauler.setAnimatedToughness(5);
        hauler.setAttacking(true);
        addCreatureReady(player1, new LeoninScimitar());

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void countsArtifactsAtResolutionEvenIfHaulerHasLeftBattlefield() {
        Permanent hauler = addCreatureReady(player1, new HoardHauler());
        hauler.setAnimatedUntilEndOfTurn(true);
        hauler.setAnimatedPower(5);
        hauler.setAnimatedToughness(5);
        hauler.setAttacking(true);
        Permanent artifact = addCreatureReady(player2, new LeoninScimitar());

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        addCreatureReady(player2, new Ornithopter());
        addCreatureReady(player2, new LeoninScimitar());
        gd.playerBattlefields.get(player1.getId()).remove(hauler);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void crewTapsSummoningSickCreaturesAndAnimatesHauler() {
        Permanent hauler = addCreatureReady(player1, new HoardHauler());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setSummoningSick(true);
        second.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, hauler)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hauler)).isTrue();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();
        harness.assertLife(player2, 15);
    }

    @Test
    void crewRejectsInsufficientPower() {
        Permanent hauler = addCreatureReady(player1, new HoardHauler());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bear.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, hauler)).isFalse();
    }
}
