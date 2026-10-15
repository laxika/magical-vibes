package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrescendoOfWar.class, GrizzlyBears.class, Opalescence.class})
class CrescendoOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a strife counter on itself at the beginning of each upkeep")
    void addsStrifeCounterAtEachUpkeep() {
        Permanent crescendo = harness.addToBattlefieldAndReturn(player1, new CrescendoOfWar());

        resolveUpkeepTrigger(player1);
        resolveUpkeepTrigger(player2);

        assertThat(crescendo.getCounterCount(CounterType.STRIFE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking creatures get +1/+0 for each strife counter")
    void boostsAttackingCreatures() {
        Permanent crescendo = harness.addToBattlefieldAndReturn(player1, new CrescendoOfWar());
        crescendo.setCounterCount(CounterType.STRIFE, 2);
        Permanent ownAttacker = addReadyCreature(player1);
        Permanent ownIdle = addReadyCreature(player1);
        Permanent opponentAttacker = addReadyCreature(player2);
        ownAttacker.setAttacking(true);
        opponentAttacker.setAttacking(true);

        assertThat(gqs.computeStaticBonus(gd, ownAttacker).power()).isEqualTo(2);
        assertThat(gqs.computeStaticBonus(gd, opponentAttacker).power()).isEqualTo(2);
        assertThat(gqs.computeStaticBonus(gd, ownIdle).power()).isZero();
    }

    @Test
    @DisplayName("Blocking creatures you control get +1/+0 for each strife counter")
    void boostsOwnBlockingCreatures() {
        Permanent crescendo = harness.addToBattlefieldAndReturn(player1, new CrescendoOfWar());
        crescendo.setCounterCount(CounterType.STRIFE, 3);
        Permanent ownBlocker = addReadyCreature(player1);
        Permanent ownIdle = addReadyCreature(player1);
        Permanent opponentBlocker = addReadyCreature(player2);
        ownBlocker.setBlocking(true);
        opponentBlocker.setBlocking(true);

        assertThat(gqs.computeStaticBonus(gd, ownBlocker).power()).isEqualTo(3);
        assertThat(gqs.computeStaticBonus(gd, opponentBlocker).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, ownIdle).power()).isZero();
    }

    private void resolveUpkeepTrigger(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Bonuses track strife counters and combat status without changing toughness")
    void bonusesTrackCountersAndCombatStatus() {
        Permanent crescendo = harness.addToBattlefieldAndReturn(player1, new CrescendoOfWar());
        Permanent creature = addReadyCreature(player1);
        creature.setAttacking(true);

        assertThat(gqs.computeStaticBonus(gd, creature).power()).isZero();
        crescendo.setCounterCount(CounterType.STRIFE, 2);
        assertThat(gqs.computeStaticBonus(gd, creature).power()).isEqualTo(2);
        assertThat(gqs.computeStaticBonus(gd, creature).toughness()).isZero();
        crescendo.setCounterCount(CounterType.STRIFE, 1);
        assertThat(gqs.computeStaticBonus(gd, creature).power()).isEqualTo(1);
        creature.setAttacking(false);
        assertThat(gqs.computeStaticBonus(gd, creature).power()).isZero();
        creature.setBlocking(true);
        assertThat(gqs.computeStaticBonus(gd, creature).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, creature).toughness()).isZero();
        creature.setBlocking(false);
        assertThat(gqs.computeStaticBonus(gd, creature).power()).isZero();
    }

    @Test
    @DisplayName("Copies add their attacking bonuses but only boost their own blockers")
    void copiesUseIndependentCountersAndControllers() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CrescendoOfWar());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CrescendoOfWar());
        first.setCounterCount(CounterType.STRIFE, 2);
        second.setCounterCount(CounterType.STRIFE, 3);
        Permanent attacker = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        attacker.setAttacking(true);
        blocker.setBlocking(true);

        assertThat(gqs.computeStaticBonus(gd, attacker).power()).isEqualTo(5);
        assertThat(gqs.computeStaticBonus(gd, blocker).power()).isEqualTo(3);

        gd.playerBattlefields.get(player2.getId()).remove(second);

        assertThat(gqs.computeStaticBonus(gd, attacker).power()).isEqualTo(2);
        assertThat(gqs.computeStaticBonus(gd, blocker).power()).isZero();
    }

    @Test
    @DisplayName("An animated Crescendo of War receives its own attacking bonus")
    void animatedCrescendoBoostsItselfWhenAttacking() {
        Permanent crescendo = harness.addToBattlefieldAndReturn(player1, new CrescendoOfWar());
        harness.addToBattlefield(player1, new Opalescence());
        crescendo.setCounterCount(CounterType.STRIFE, 2);
        crescendo.setAttacking(true);

        assertThat(gqs.isCreature(gd, crescendo)).isTrue();
        assertThat(gqs.getEffectivePower(gd, crescendo)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, crescendo)).isEqualTo(4);
    }

    @Test
    @DisplayName("An animated Crescendo of War receives its own blocking bonus")
    void animatedCrescendoBoostsItselfWhenBlocking() {
        Permanent crescendo = harness.addToBattlefieldAndReturn(player1, new CrescendoOfWar());
        harness.addToBattlefield(player1, new Opalescence());
        crescendo.setCounterCount(CounterType.STRIFE, 2);
        crescendo.setBlocking(true);

        assertThat(gqs.isCreature(gd, crescendo)).isTrue();
        assertThat(gqs.getEffectivePower(gd, crescendo)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, crescendo)).isEqualTo(4);
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
