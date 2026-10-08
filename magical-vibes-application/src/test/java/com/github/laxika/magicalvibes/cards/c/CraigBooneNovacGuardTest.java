package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CraigBooneNovacGuard.class, GrizzlyBears.class})
class CraigBooneNovacGuardTest extends BaseCardTest {

    @Test
    void hasReachAndLifelinkAndNeedsTwoAttackers() {
        Permanent craig = addCreatureReady(player1, new CraigBooneNovacGuard());
        addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, craig, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, craig, Keyword.LIFELINK)).isTrue();

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(craig.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void putsQuestCountersThenLetsControllerChooseDamageMode() {
        Permanent craig = addCreatureReady(player1, new CraigBooneNovacGuard());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(craig.getCounterCount(CounterType.QUEST)).isEqualTo(2);
        harness.assertLife(player2, 18);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void decliningPlayerDamageDealsDamageEqualToCurrentQuestCounters() {
        Permanent craig = addCreatureReady(player1, new CraigBooneNovacGuard());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(craig.getCounterCount(CounterType.QUEST)).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void canDeclineTheOptionalCreatureTarget() {
        Permanent craig = addCreatureReady(player1, new CraigBooneNovacGuard());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(craig.getCounterCount(CounterType.QUEST)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void damageUsesQuestCounterCountWhenReflexiveAbilityResolves() {
        Permanent craig = addCreatureReady(player1, new CraigBooneNovacGuard());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        craig.setCounterCount(CounterType.QUEST, 5);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 25);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void damageUsesLastKnownQuestCountersAfterCraigLeavesBattlefield() {
        Permanent craig = addCreatureReady(player1, new CraigBooneNovacGuard());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        craig.setCounterCount(CounterType.QUEST, 3);

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        craig.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Craig Boone, Novac Guard");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 25);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void canTargetOwnCreatureAndGainLifeFromCreatureDamage() {
        Permanent craig = addCreatureReady(player1, new CraigBooneNovacGuard());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, craig.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(craig.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void threeAttackersStillPutOnlyTwoQuestCounters() {
        Permanent craig = addCreatureReady(player1, new CraigBooneNovacGuard());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2, 3));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());

        harness.passBothPriorities();

        assertThat(craig.getCounterCount(CounterType.QUEST)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsAttackDoesNotPutQuestCountersOnCraig() {
        Permanent craig = addCreatureReady(player1, new CraigBooneNovacGuard());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(craig.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
