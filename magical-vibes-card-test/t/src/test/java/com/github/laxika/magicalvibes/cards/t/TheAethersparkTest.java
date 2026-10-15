package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BrightfieldMustang;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheAetherspark.class, BrightfieldMustang.class})
class TheAethersparkTest extends BaseCardTest {

    @Test
    void plusOneAttachesAndPutsCounterOnCreature() {
        Permanent spark = addAetherspark(player1, 4);
        Permanent creature = addCreatureReady(player1, new BrightfieldMustang());

        harness.activateAbility(player1, battlefieldIndex(player1, spark), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(spark.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void plusOneCanResolveWithoutTarget() {
        Permanent spark = addAetherspark(player1, 4);

        harness.activateAbility(player1, battlefieldIndex(player1, spark), 0, null, (UUID) null);
        harness.passBothPriorities();

        assertThat(spark.getAttachedTo()).isNull();
        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void combatDamageDuringControllerTurnAddsThatManyLoyalty() {
        Permanent spark = addAetherspark(player1, 4);
        Permanent creature = addCreatureReady(player1, new BrightfieldMustang());
        spark.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        harness.assertLife(player2, 17);
    }

    @Test
    void combatDamageDuringAnotherPlayersTurnDoesNotTrigger() {
        Permanent spark = addAetherspark(player1, 4);
        Permanent creature = addCreatureReady(player2, new BrightfieldMustang());
        spark.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertLife(player1, 17);
    }

    @Test
    void attachedSparkCannotBeDeclaredAsAnAttackTarget() {
        Permanent spark = addAetherspark(player2, 4);
        Permanent host = addCreatureReady(player2, new BrightfieldMustang());
        spark.setAttachedTo(host.getId());
        Permanent attacker = addCreatureReady(player1, new BrightfieldMustang());
        prepareAttackDeclaration(player1);

        assertThat(als.getValidAttackTargetIds(gd, player1.getId())).doesNotContain(spark.getId());
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0), Map.of(0, spark.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(attacker.getAttackTarget()).isNull();
    }

    @Test
    void unattachedSparkCanBeDeclaredAsAnAttackTarget() {
        Permanent spark = addAetherspark(player2, 4);
        addCreatureReady(player1, new BrightfieldMustang());
        prepareAttackDeclaration(player1);

        assertThat(als.getValidAttackTargetIds(gd, player1.getId())).contains(spark.getId());
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, spark.getId()));
    }

    @Test
    void minusFiveDrawsTwoCards() {
        Permanent spark = addAetherspark(player1, 6);
        harness.setLibrary(player1, List.of(new BrightfieldMustang(), new BrightfieldMustang()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, battlefieldIndex(player1, spark), 1, null, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void minusTenAddsTenManaOfChosenColor() {
        Permanent spark = addAetherspark(player1, 10);

        harness.activateAbility(player1, battlefieldIndex(player1, spark), 2, null, (UUID) null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(10);
    }

    @Test
    void plusOneCanTargetTheAlreadyEquippedCreature() {
        Permanent spark = addAetherspark(player1, 4);
        Permanent creature = addCreatureReady(player1, new BrightfieldMustang());
        spark.setAttachedTo(creature.getId());
        long originalTimestamp = spark.getTimestamp();

        harness.activateAbility(player1, battlefieldIndex(player1, spark), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(spark.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(spark.getTimestamp()).isEqualTo(originalTimestamp);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void plusOneMovesToAnotherCreature() {
        Permanent spark = addAetherspark(player1, 4);
        Permanent oldHost = addCreatureReady(player1, new BrightfieldMustang());
        Permanent newHost = addCreatureReady(player1, new BrightfieldMustang());
        spark.setAttachedTo(oldHost.getId());

        harness.activateAbility(player1, battlefieldIndex(player1, spark), 0, null, newHost.getId());
        harness.passBothPriorities();

        assertThat(spark.getAttachedTo()).isEqualTo(newHost.getId());
        assertThat(oldHost.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(newHost.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void plusOneCannotTargetAnOpponentsCreature() {
        Permanent spark = addAetherspark(player1, 4);
        Permanent creature = addCreatureReady(player2, new BrightfieldMustang());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                battlefieldIndex(player1, spark), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(spark.getAttachedTo()).isNull();
    }

    @Test
    void combatDamageToBlockingCreatureAddsLoyaltyEvenWhenHostDies() {
        Permanent spark = addAetherspark(player1, 4);
        Permanent creature = addCreatureReady(player1, new BrightfieldMustang());
        spark.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BrightfieldMustang());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(battlefieldIndex(player1, creature));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(spark.getAttachedTo()).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    void unattachedSparkDoesNotGainLoyaltyFromCombatDamage() {
        Permanent spark = addAetherspark(player1, 4);
        addCreatureReady(player1, new BrightfieldMustang()).setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertLife(player2, 17);
    }

    @Test
    void minusFiveAtExactlyFiveLoyaltyStillDrawsAfterSparkDies() {
        Permanent spark = addAetherspark(player1, 5);
        harness.setLibrary(player1, List.of(new BrightfieldMustang(), new BrightfieldMustang()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, battlefieldIndex(player1, spark), 1, null, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spark);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
    }

    @Test
    void minusTenUsesTheStackInsteadOfResolvingAsAManaAbility() {
        Permanent spark = addAetherspark(player1, 11);

        harness.activateAbility(player1, battlefieldIndex(player1, spark), 2, null, (UUID) null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(10);
        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void plusOneWithNoTargetKeepsItsExistingAttachment() {
        Permanent spark = addAetherspark(player1, 4);
        Permanent host = addCreatureReady(player1, new BrightfieldMustang());
        spark.setAttachedTo(host.getId());

        harness.activateAbility(player1, battlefieldIndex(player1, spark), 0, null, (UUID) null);
        harness.passBothPriorities();

        assertThat(spark.getAttachedTo()).isEqualTo(host.getId());
        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void cannotPayMoreLoyaltyThanAvailable() {
        Permanent spark = addAetherspark(player1, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                battlefieldIndex(player1, spark), 1, null, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attachingAfterAttackDeclarationDoesNotStopExistingAttack() {
        Permanent spark = addAetherspark(player2, 4);
        Permanent host = addCreatureReady(player2, new BrightfieldMustang());
        Permanent attacker = addCreatureReady(player1, new BrightfieldMustang());
        prepareAttackDeclaration(player1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(0), Map.of(0, spark.getId())));

        spark.setAttachedTo(host.getId());
        assertThat(attacker.getAttackTarget()).isEqualTo(spark.getId());
        resolveCombat();
        harness.passBothPriorities();

        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    private Permanent addAetherspark(Player player, int loyalty) {
        Permanent spark = harness.addToBattlefieldAndReturn(player, new TheAetherspark());
        spark.setCounterCount(CounterType.LOYALTY, loyalty);
        spark.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return spark;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void prepareAttackDeclaration(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
