package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.g.GlacialWall;
import com.github.laxika.magicalvibes.cards.j.JohtullWurm;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadWight.class, BalduvianBears.class, GlacialWall.class, JohtullWurm.class, Solemnity.class})
class DreadWightTest extends BaseCardTest {

    @Test
    @DisplayName("A blocker is not paralyzed before end of combat")
    void blockerIsNotParalyzedBeforeEndOfCombat() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GlacialWall());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isZero();
        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("When blocked by multiple creatures, each blocker is paralyzed at end of combat")
    void eachBlockerIsParalyzed() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent firstWall = addCreatureReady(player2, new GlacialWall());
        Permanent secondWall = addCreatureReady(player2, new GlacialWall());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, java.util.Map.of(firstWall.getId(), 3));

        leaveEndOfCombat();

        assertThat(firstWall.getCounterCount(CounterType.PARALYZATION)).isEqualTo(1);
        assertThat(secondWall.getCounterCount(CounterType.PARALYZATION)).isEqualTo(1);
        assertThat(firstWall.isTapped()).isTrue();
        assertThat(secondWall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At end of combat the blocker gets a paralyzation counter, is tapped, and gains the remove ability")
    void blockerParalyzedAtEndOfCombat() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GlacialWall());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        leaveEndOfCombat();

        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isEqualTo(1);
        assertThat(spider.isTapped()).isTrue();
        activateRemovalAbility(spider);
        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isZero();
    }

    @Test
    @DisplayName("Even when the counter cannot be placed, the creature is still tapped")
    void stillTapsWhenCounterCannotBePlaced() {
        Permanent blocker = resolveSolemnityCombat();

        assertThat(blocker.getCounterCount(CounterType.PARALYZATION)).isZero();
        assertThat(blocker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Even when the counter cannot be placed, the creature still gains the removal ability")
    void stillGrantsRemovalAbilityWhenCounterCannotBePlaced() {
        Permanent blocker = resolveSolemnityCombat();

        activateRemovalAbility(blocker);
        assertThat(blocker.getCounterCount(CounterType.PARALYZATION)).isZero();
    }

    private Permanent resolveSolemnityCombat() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GlacialWall());
        harness.addToBattlefield(player1, new Solemnity());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        leaveEndOfCombat();
        return blocker;
    }

    @Test
    @DisplayName("When Dread Wight blocks an attacker, that attacker is paralyzed at end of combat")
    void blocksAttackerParalyzes() {
        Permanent attacker = addCreatureReady(player1, new DreadWight());
        attacker.setAttacking(true);
        addCreatureReady(player2, new DreadWight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        leaveEndOfCombat();

        assertThat(attacker.getCounterCount(CounterType.PARALYZATION)).isEqualTo(1);
        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not paralyze an attacker when Dread Wight dies before end of combat")
    void doesNotParalyzeWhenSourceDiesBeforeEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new JohtullWurm());
        attacker.setAttacking(true);
        Permanent wight = addCreatureReady(player2, new DreadWight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wight);

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PARALYZATION)).isZero();
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not paralyze a blocker when Dread Wight dies before end of combat")
    void doesNotParalyzeBlockerWhenSourceDiesBeforeEndOfCombat() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new JohtullWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wight);

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.PARALYZATION)).isZero();
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paralyzing a creature with a different removal ability still grants the required ability")
    void grantsExactRemovalAbilityWhenDifferentAmountAlreadyExists() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GlacialWall());
        spider.getPersistentGrantedActivatedAbilities().add(new ActivatedAbility(
                false,
                "{8}",
                List.of(new RemoveCounterFromSourceEffect(CounterType.PARALYZATION, 2)),
                "{8}: Remove two paralyzation counters from this creature."));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        leaveEndOfCombat();

        spider.setCounterCount(CounterType.PARALYZATION, 2);
        activateRemovalAbility(spider);
        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with a paralyzation counter does not untap during its controller's untap step")
    void doesNotUntapWhileParalyzed() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GlacialWall());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        leaveEndOfCombat();

        advanceToUpkeep(player2);

        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("After removing the last paralyzation counter, the creature untaps on the next untap step")
    void untapsAfterCounterRemoved() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GlacialWall());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        leaveEndOfCombat();

        spider.tap();

        activateRemovalAbility(spider);

        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isZero();

        advanceToUpkeep(player2);

        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does nothing when Dread Wight neither blocks nor is blocked")
    void noEffectWhenNotInCombat() {
        addCreatureReady(player1, new DreadWight());
        Permanent spider = addCreatureReady(player2, new GlacialWall());

        leaveEndOfCombat();

        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isZero();
        assertThat(spider.isTapped()).isFalse();
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.withAutoStop(TurnStep.END_OF_COMBAT, this::resolveAllTriggers);
    }

    private void activateRemovalAbility(Permanent creature) {
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int permanentIndex = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        int abilityIndex = gs.getEffectiveActivatedAbilities(gd, creature).size() - 1;
        harness.activateAbility(player2, permanentIndex, abilityIndex, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Blocking does not trigger Dread Wight until the end of combat step begins")
    void blockingDoesNotTriggerBeforeEndOfCombat() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        addCreatureReady(player2, new GlacialWall());

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The end-of-combat ability uses the stack and resolves even if Dread Wight then leaves")
    void endOfCombatAbilityUsesStack() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        Permanent wall = addCreatureReady(player2, new GlacialWall());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wight);
        assertThat(wall.getCounterCount(CounterType.PARALYZATION)).isZero();
        assertThat(wall.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(wight);
        resolveAllTriggers();

        assertThat(wall.getCounterCount(CounterType.PARALYZATION)).isEqualTo(1);
        assertThat(wall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A paralyzation counter alone does not prevent untapping")
    void counterWithoutDreadWightEffectDoesNotPreventUntapping() {
        Permanent bears = addCreatureReady(player2, new GlacialWall());
        bears.setCounterCount(CounterType.PARALYZATION, 1);
        bears.tap();

        advanceToUpkeep(player2);

        assertThat(bears.isTapped()).isFalse();
        assertThat(bears.getCounterCount(CounterType.PARALYZATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("After the last counter is removed, a later counter does not restart the expired untap restriction")
    void laterCounterDoesNotRestartExpiredRestriction() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new GlacialWall());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        leaveEndOfCombat();
        activateRemovalAbility(wall);
        assertThat(wall.getCounterCount(CounterType.PARALYZATION)).isZero();

        wall.setCounterCount(CounterType.PARALYZATION, 1);
        wall.tap();
        advanceToUpkeep(player2);

        assertThat(wall.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap restriction never starts if no paralyzation counter could be placed")
    void preventedCounterDoesNotCreateFutureUntapRestriction() {
        Permanent blocker = resolveSolemnityCombat();
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof Solemnity);
        blocker.setCounterCount(CounterType.PARALYZATION, 1);

        advanceToUpkeep(player2);

        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The paralyzation effect persists after Dread Wight leaves the battlefield")
    void effectPersistsAfterDreadWightLeaves() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new GlacialWall());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        leaveEndOfCombat();
        gd.playerBattlefields.get(player1.getId()).remove(wight);

        advanceToUpkeep(player2);
        assertThat(wall.isTapped()).isTrue();

        activateRemovalAbility(wall);
        advanceToUpkeep(player2);
        assertThat(wall.isTapped()).isFalse();
    }

}
