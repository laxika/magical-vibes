package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfPredation.class, PhyrexianRager.class, JaceBeleren.class})
class CurseOfPredationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Curse of Predation attaches it to the target player")
    void attachesToTargetPlayer() {
        harness.setHand(player1, List.of(new CurseOfPredation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Curse of Predation")
                        && permanent.getAttachedTo().equals(player2.getId()));
    }

    @Test
    @DisplayName("A creature attacking the enchanted player gets a +1/+1 counter")
    void attackingCreatureGetsCounter() {
        addCurseOnPlayer2();
        Permanent attacker = addCreatureReady(player1, new PhyrexianRager());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each creature attacking the enchanted player gets its own counter")
    void eachAttackerGetsCounter() {
        addCurseOnPlayer2();
        Permanent firstAttacker = addCreatureReady(player1, new PhyrexianRager());
        Permanent secondAttacker = addCreatureReady(player1, new PhyrexianRager());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(firstAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature attacking the enchanted player's planeswalker does not trigger the Curse")
    void attackOnDifferentPlayerDoesNotTrigger() {
        addCurseOnPlayer2();
        Permanent attacker = addCreatureReady(player1, new PhyrexianRager());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(1), Map.of(1, planeswalker.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A Curse enchanting its controller benefits the opponent's attacker")
    void opponentAttackingCurseControllerGetsCounter() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfPredation());
        curse.setAttachedTo(player1.getId());
        Permanent attacker = addCreatureReady(player2, new PhyrexianRager());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Curses on the same player each add a counter")
    void multipleCursesEachAddCounter() {
        addCurseOnPlayer2();
        addCurseOnPlayer2();
        Permanent attacker = addCreatureReady(player1, new PhyrexianRager());

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking a player who is not enchanted adds no counter")
    void attackingUnenchantedPlayerAddsNoCounter() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfPredation());
        curse.setAttachedTo(player1.getId());
        Permanent attacker = addCreatureReady(player1, new PhyrexianRager());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Removing the Curse does not stop its already triggered ability")
    void triggerResolvesAfterCurseLeavesBattlefield() {
        addCurseOnPlayer2();
        Permanent curse = findPermanent(player1, "Curse of Predation");
        Permanent attacker = addCreatureReady(player1, new PhyrexianRager());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(curse);
        gd.playerGraveyards.get(player1.getId()).add(curse.getCard());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void addCurseOnPlayer2() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfPredation());
        curse.setAttachedTo(player2.getId());
    }
}
