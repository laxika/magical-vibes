package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BasriKetTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts a counter on a creature and grants it indestructible")
    void plusOneCountersAndProtectsTargetCreature() {
        addReadyBasri(3);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("-2 creates one tapped and attacking Soldier for each nontoken attacker")
    void minusTwoCountsOnlyNontokenAttackers() {
        addReadyBasri(3);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        addCreatureReady(player1, tokenCard);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getCard().isToken()).isTrue();
            assertThat(soldier.isTapped()).isTrue();
            assertThat(soldier.isAttacking()).isTrue();
            assertThat(soldier.getAttackTarget()).isEqualTo(player2.getId());
        });
    }

    @Test
    @DisplayName("-6 creates an emblem that triggers at the beginning of combat")
    void ultimateEmblemCreatesAndCountersSoldier() {
        addReadyBasri(6);
        Permanent existingCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(1);
        assertThat(soldiers.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(existingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyBasri(int loyalty) {
        Permanent permanent = new Permanent(new BasriKet());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

}
