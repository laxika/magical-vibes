package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.p.PlagueMyr;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChokingFumes.class, LeoninSkyhunter.class, PlagueMyr.class})
class ChokingFumesTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -1/-1 counter on each attacking creature")
    void putsCounterOnEachAttackingCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        bears.setSummoningSick(false);
        bears.setAttacking(true);

        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        bears2.setSummoningSick(false);
        bears2.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChokingFumes()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(bears2.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not affect non-attacking creatures")
    void doesNotAffectNonAttackingCreatures() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        nonAttacker.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChokingFumes()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(nonAttacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Kills a 1/1 attacking creature with the -1/-1 counter")
    void killsOneOneAttacker() {
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new PlagueMyr());
        elf.setSummoningSick(false);
        elf.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChokingFumes()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player2, "Plague Myr");
        harness.assertInGraveyard(player2, "Plague Myr");
    }

    @Test
    @DisplayName("Also affects the caster's own attacking creatures")
    void affectsCastersOwnAttackers() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent defender = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChokingFumes()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(defender.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does nothing when no creatures are attacking")
    void doesNothingWithNoAttackers() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChokingFumes()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not put counters on blocking creatures")
    void doesNotAffectBlockers() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChokingFumes()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Checks attacking status when the spell resolves")
    void checksAttackingStatusAtResolution() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChokingFumes()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);
        harness.castInstant(player1, 0);

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Choking Fumes");
    }
}
