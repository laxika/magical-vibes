package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SilverKnight;
import com.github.laxika.magicalvibes.cards.s.Stabilizer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsumptiveGoo.class, SilverKnight.class, Stabilizer.class})
class ConsumptiveGooTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability shrinks a target creature and puts a +1/+1 counter on Consumptive Goo")
    void shrinksTargetAndPutsCounterOnSelf() {
        Permanent goo = addCreatureReady(player1, new ConsumptiveGoo());
        Permanent bears = addCreatureReady(player2, new SilverKnight());

        activateGoo(goo, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(goo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, goo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goo)).isEqualTo(2);
    }

    @Test
    @DisplayName("The -1/-1 effect can kill a 1/1 target while the counter is placed on the source")
    void killsOneOneTargetAndCountersSource() {
        Permanent goo = addCreatureReady(player1, new ConsumptiveGoo());
        Permanent target = addCreatureReady(player2, new ConsumptiveGoo());

        activateGoo(goo, target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getOriginalCard());
        assertThat(goo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The target creature shrink wears off at end of turn while the counter remains")
    void shrinkWearsOffAtEndOfTurn() {
        Permanent goo = addCreatureReady(player1, new ConsumptiveGoo());
        Permanent bears = addCreatureReady(player2, new SilverKnight());

        activateGoo(goo, bears);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(goo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent goo = addCreatureReady(player1, new ConsumptiveGoo());
        Permanent stabilizer = harness.addToBattlefieldAndReturn(player2, new Stabilizer());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(goo),
                null,
                stabilizer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activateGoo(Permanent goo, Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(goo),
                null,
                target.getId());
        harness.passBothPriorities();
    }
}
