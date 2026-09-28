package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SierraNukasBiggestFan.class, GrizzlyBears.class})
class SierraNukasBiggestFanTest extends BaseCardTest {

    @Test
    void oneOrMoreCreaturesDealingCombatDamageAddsOneQuestCounterAndFood() {
        Permanent sierra = addCreatureReady(player1, new SierraNukasBiggestFan());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(sierra.getCounterCount(CounterType.QUEST)).isOne();
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    void sacrificingFoodBoostsTargetCreatureBySierraQuestCounters() {
        Permanent sierra = addCreatureReady(player1, new SierraNukasBiggestFan());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        Permanent food = findPermanent(player1, "Food");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(sierra.getCounterCount(CounterType.QUEST)).isOne();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(countPermanents(player1, "Food")).isZero();
    }
}
