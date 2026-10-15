package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.t.ThrabenWatcher;
import com.github.laxika.magicalvibes.cards.w.WorldWeary;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BottleGolems.class, DiabolicEdict.class, ThrabenWatcher.class, WorldWeary.class})
class BottleGolemsTest extends BaseCardTest {

    @Test
    void gainsLifeEqualToItsPowerWhenItDies() {
        Permanent golems = harness.addToBattlefieldAndReturn(player1, new BottleGolems());
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(golems);
    }

    @Test
    void usesItsLastKnownPowerWhenItDies() {
        Permanent golems = harness.addToBattlefieldAndReturn(player1, new BottleGolems());
        golems.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    void includesContinuousPowerBonusesInLifeGained() {
        harness.addToBattlefield(player1, new ThrabenWatcher());
        Permanent golems = harness.addToBattlefieldAndReturn(player1, new BottleGolems());
        golems.setMarkedDamage(4);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Bottle Golems");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    void gainsNoLifeWhenItsLastKnownPowerIsNegative() {
        Permanent golems = harness.addToBattlefieldAndReturn(player1, new BottleGolems());
        harness.setHand(player1, List.of(new WorldWeary()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, golems.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Bottle Golems");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void gainsNoLifeWhenItsLastKnownPowerIsZero() {
        Permanent golems = harness.addToBattlefieldAndReturn(player1, new BottleGolems());
        golems.setCounterCount(CounterType.MINUS_ONE_MINUS_ZERO, 3);
        golems.setMarkedDamage(3);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Bottle Golems");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void opponentGainsLifeWhenTheirGolemsDies() {
        Permanent golems = harness.addToBattlefieldAndReturn(player2, new BottleGolems());
        golems.setMarkedDamage(3);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Bottle Golems");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
    }
}
