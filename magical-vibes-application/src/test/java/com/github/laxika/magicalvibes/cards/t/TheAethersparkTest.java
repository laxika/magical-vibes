package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheAethersparkTest extends BaseCardTest {

    @Test
    void plusOneAttachesAndPutsCounterOnCreature() {
        Permanent spark = addAetherspark(player1, 4);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

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
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        spark.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void combatDamageDuringAnotherPlayersTurnDoesNotTrigger() {
        Permanent spark = addAetherspark(player1, 4);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        spark.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(spark.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void attachedSparkCannotBeDeclaredAsAnAttackTarget() {
        Permanent spark = addAetherspark(player2, 4);
        Permanent host = addCreatureReady(player2, new GrizzlyBears());
        spark.setAttachedTo(host.getId());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        prepareAttackDeclaration(player1);

        assertThat(als.getValidAttackTargetIds(gd, player1.getId())).doesNotContain(spark.getId());
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0), Map.of(0, spark.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(attacker.getAttackTarget()).isNull();
    }

    @Test
    void unattachedSparkCanBeDeclaredAsAnAttackTarget() {
        Permanent spark = addAetherspark(player2, 4);
        addCreatureReady(player1, new GrizzlyBears());
        prepareAttackDeclaration(player1);

        assertThat(als.getValidAttackTargetIds(gd, player1.getId())).contains(spark.getId());
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, spark.getId()));
    }

    @Test
    void minusFiveDrawsTwoCards() {
        Permanent spark = addAetherspark(player1, 6);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
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

    private Permanent addAetherspark(Player player, int loyalty) {
        Permanent spark = new Permanent(new TheAetherspark());
        spark.setCounterCount(CounterType.LOYALTY, loyalty);
        spark.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(spark);
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
