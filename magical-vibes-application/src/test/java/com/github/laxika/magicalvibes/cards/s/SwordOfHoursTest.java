package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagneticTheft;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD12AndResolveIfGreaterThanEventValueEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwordOfHours.class, GrizzlyBears.class, MagneticTheft.class})
class SwordOfHoursTest extends BaseCardTest {

    private RollD12AndResolveIfGreaterThanEventValueEffectHandler rollHandler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        rollHandler = GameTestEngineContext.get()
                .getBean(RollD12AndResolveIfGreaterThanEventValueEffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(
                rollHandler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(rollHandler, "diceRollService", originalDiceRollService);
    }

    @Test
    void equippedCreatureGetsCounterWhenAttacking() {
        Permanent creature = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void rollGreaterThanDamageDoublesCounters() {
        Permanent creature = prepareCombat(5);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void naturalTwelveDoublesCountersEvenWhenNotGreaterThanDamage() {
        Permanent creature = prepareCombat(12);
        creature.setPowerModifier(8);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void rollEqualToDamageDoesNotDoubleCounters() {
        Permanent creature = prepareCombat(4);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void rollBelowDamageDoesNotDoubleCounters() {
        Permanent creature = prepareCombat(3);

        resolveCombat();
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void successfulRollWithNoCountersDoesNotCreateCounters() {
        Permanent creature = prepareCombat(12);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gameLogContains("rolls a d12 for Sword of Hours: 12")).isTrue();
    }

    @Test
    void doublesTheCountersPresentWhenTheDamageTriggerResolves() {
        Permanent creature = prepareCombat(12);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void combatDamageToABlockingCreatureTriggersTheRoll() {
        Permanent creature = prepareCombat(5);
        addCreatureReady(player2);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertLife(player2, 20);
    }

    @Test
    void stillRollsWhenTheEquippedCreatureDiesDealingCombatDamage() {
        Permanent creature = prepareCombat(12);
        Permanent blocker = addCreatureReady(player2);
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gameLogContains("rolls a d12 for Sword of Hours: 12")).isTrue();
    }

    @Test
    void attackCounterStaysWithTheAttackerWhenSwordMovesInResponse() {
        Permanent attacker = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        Permanent otherCreature = addCreatureReady(player1);
        sword.setAttachedTo(attacker.getId());
        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        harness.castAndResolveInstant(player1, 0, List.of(sword.getId(), otherCreature.getId()));
        resolveAllTriggers();

        assertThat(sword.getAttachedTo()).isEqualTo(otherCreature.getId());
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void damageCounterDoublingStaysWithTheDamageDealerWhenSwordMovesInResponse() {
        Permanent creature = prepareCombat(12);
        Permanent sword = findPermanent(player1, "Sword of Hours");
        Permanent otherCreature = addCreatureReady(player1);
        otherCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.castAndResolveInstant(player1, 0, List.of(sword.getId(), otherCreature.getId()));
        resolveAllTriggers();

        assertThat(sword.getAttachedTo()).isEqualTo(otherCreature.getId());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void equipAttachesSwordForTwoMana() {
        Permanent creature = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2);
        addSwordReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent creature = addCreatureReady(player1);
        addSwordReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent prepareCombat(int roll) {
        ReflectionTestUtils.setField(rollHandler, "diceRollService", new FixedDiceRollService(roll));

        Permanent creature = addCreatureReady(player1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        return creature;
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addSwordReady(Player player) {
        Permanent sword = harness.addToBattlefieldAndReturn(player, new SwordOfHours());
        sword.setSummoningSick(false);
        return sword;
    }

    private static final class FixedDiceRollService extends DiceRollService {

        private final int result;

        private FixedDiceRollService(int result) {
            this.result = result;
        }

        @Override
        public int roll(int sides) {
            return result;
        }
    }
}
