package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClericOfLifesBond.class, ClericOfChillDepths.class, GrizzlyBears.class})
class ClericOfLifesBondTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another Cleric you control enters")
    void gainsLifeWhenAnotherClericEnters() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ClericOfLifesBond());
        harness.setHand(player1, List.of(new ClericOfChillDepths()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not trigger when a non-Cleric enters")
    void doesNotTriggerForNonCleric() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ClericOfLifesBond());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on the first life gain each turn")
    void putsCounterOnFirstLifeGainEachTurn() {
        harness.addToBattlefield(player1, new ClericOfLifesBond());
        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        });
        harness.passBothPriorities();

        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
