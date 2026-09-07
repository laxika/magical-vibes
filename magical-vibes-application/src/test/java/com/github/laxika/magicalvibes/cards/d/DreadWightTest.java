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
        Permanent spider = addCreatureReady(player2, new BalduvianBears());

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
        Permanent spider = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        leaveEndOfCombat();

        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isEqualTo(1);
        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getPersistentGrantedActivatedAbilities()).hasSize(1);
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

        assertThat(blocker.getPersistentGrantedActivatedAbilities())
                .flatExtracting(ActivatedAbility::getEffects)
                .contains(new RemoveCounterFromSourceEffect(CounterType.PARALYZATION, 1));
    }

    private Permanent resolveSolemnityCombat() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
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
        Permanent spider = addCreatureReady(player2, new BalduvianBears());
        spider.getPersistentGrantedActivatedAbilities().add(new ActivatedAbility(
                false,
                "{8}",
                List.of(new RemoveCounterFromSourceEffect(CounterType.PARALYZATION, 2)),
                "{8}: Remove two paralyzation counters from this creature."));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        leaveEndOfCombat();

        assertThat(spider.getPersistentGrantedActivatedAbilities())
                .flatExtracting(ActivatedAbility::getEffects)
                .filteredOn(RemoveCounterFromSourceEffect.class::isInstance)
                .containsExactlyInAnyOrder(
                        new RemoveCounterFromSourceEffect(CounterType.PARALYZATION, 2),
                        new RemoveCounterFromSourceEffect(CounterType.PARALYZATION, 1));
    }

    @Test
    @DisplayName("A creature with a paralyzation counter does not untap during its controller's untap step")
    void doesNotUntapWhileParalyzed() {
        Permanent spider = addCreatureReady(player2, new BalduvianBears());
        spider.setCounterCount(CounterType.PARALYZATION, 1);
        spider.tap();

        advanceToUpkeep(player2);

        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("After removing the last paralyzation counter, the creature untaps on the next untap step")
    void untapsAfterCounterRemoved() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        leaveEndOfCombat();

        spider.tap();

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        int spiderIndex = gd.playerBattlefields.get(player2.getId()).indexOf(spider);
        int abilityIndex = gs.getEffectiveActivatedAbilities(gd, spider).size() - 1;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, spiderIndex, abilityIndex, null, null);
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isZero();

        advanceToUpkeep(player2);

        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does nothing when Dread Wight neither blocks nor is blocked")
    void noEffectWhenNotInCombat() {
        addCreatureReady(player1, new DreadWight());
        Permanent spider = addCreatureReady(player2, new BalduvianBears());

        leaveEndOfCombat();

        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isZero();
        assertThat(spider.isTapped()).isFalse();
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
