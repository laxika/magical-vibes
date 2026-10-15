package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FestivalCrasher;
import com.github.laxika.magicalvibes.cards.w.WrennAndSeven;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonragersSlash.class, FestivalCrasher.class, WrennAndSeven.class})
class MoonragersSlashTest extends BaseCardTest {

    @Test
    @DisplayName("Costs only red mana and deals 3 damage at night")
    void costsLessAndDealsDamageAtNight() {
        gd.dayNight = DayNight.NIGHT;
        harness.setHand(player1, List.of(new MoonragersSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Does not receive the cost reduction when it is not night")
    void doesNotCostLessWhenNotNight() {
        harness.setHand(player1, List.of(new MoonragersSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotCostLessDuringDay() {
        gd.dayNight = DayNight.DAY;
        harness.setHand(player1, List.of(new MoonragersSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsDamageWhenFullCostIsPaidDuringDay() {
        gd.dayNight = DayNight.DAY;
        harness.setHand(player1, List.of(new MoonragersSlash()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Moonrager's Slash");
    }

    @Test
    void nightReductionDoesNotRemoveRedRequirement() {
        gd.dayNight = DayNight.NIGHT;
        harness.setHand(player1, List.of(new MoonragersSlash()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsLethalDamageToCreature() {
        gd.dayNight = DayNight.NIGHT;
        var creature = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        harness.setHand(player1, List.of(new MoonragersSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Festival Crasher");
        harness.assertInGraveyard(player2, "Festival Crasher");
    }

    @Test
    void removesThreeLoyaltyFromPlaneswalker() {
        gd.dayNight = DayNight.NIGHT;
        var planeswalker = harness.addToBattlefieldAndReturn(player2, new WrennAndSeven());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new MoonragersSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Wrenn and Seven");
    }

    @Test
    void canTargetItsController() {
        gd.dayNight = DayNight.NIGHT;
        harness.setHand(player1, List.of(new MoonragersSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }
}
