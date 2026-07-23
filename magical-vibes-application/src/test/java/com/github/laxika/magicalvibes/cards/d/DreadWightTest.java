package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DreadWightTest extends BaseCardTest {

    @Test
    @DisplayName("When Dread Wight becomes blocked, each blocker is scheduled for a paralyzation counter and tap")
    void becomesBlockedSchedulesCounterAndTap() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(PutCounterOnPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId())
                        && a.counterType() == CounterType.PARALYZATION
                        && a.alsoTap());
    }

    @Test
    @DisplayName("At end of combat the blocker gets a paralyzation counter, is tapped, and gains the remove ability")
    void blockerParalyzedAtEndOfCombat() {
        Permanent wight = addCreatureReady(player1, new DreadWight());
        wight.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        leaveEndOfCombat();

        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isEqualTo(1);
        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getPersistentGrantedActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("When Dread Wight blocks an attacker, that attacker is paralyzed at end of combat")
    void blocksAttackerParalyzes() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new DreadWight());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        leaveEndOfCombat();

        assertThat(attacker.getCounterCount(CounterType.PARALYZATION)).isEqualTo(1);
        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature with a paralyzation counter does not untap during its controller's untap step")
    void doesNotUntapWhileParalyzed() {
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        spider.setCounterCount(CounterType.PARALYZATION, 1);
        spider.tap();

        advanceToNextTurn(player1);

        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("After removing the last paralyzation counter, the creature untaps on the next untap step")
    void untapsAfterCounterRemoved() {
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        spider.setCounterCount(CounterType.PARALYZATION, 1);
        spider.getPersistentGrantedActivatedAbilities().add(new ActivatedAbility(
                false,
                "{4}",
                List.of(new RemoveCounterFromSourceEffect(CounterType.PARALYZATION, 1)),
                "{4}: Remove a paralyzation counter from this creature."));
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

        advanceToNextTurn(player1);

        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does nothing when Dread Wight neither blocks nor is blocked")
    void noEffectWhenNotInCombat() {
        addCreatureReady(player1, new DreadWight());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        leaveEndOfCombat();

        assertThat(gd.hasDelayedAction(PutCounterOnPermanentAtEndOfCombat.class)).isFalse();
        assertThat(spider.getCounterCount(CounterType.PARALYZATION)).isZero();
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
