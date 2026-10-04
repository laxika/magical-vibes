package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalScientist;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrowthCurve.class, EnvironmentalScientist.class, Island.class})
class GrowthCurveTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one +1/+1 counter, then doubles the creature's +1/+1 counters")
    void addsAndDoublesPlusOneCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EnvironmentalScientist());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 3);
        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Doubles the newly added counter when the creature had no +1/+1 counters")
    void doublesNewCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EnvironmentalScientist());
        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target only a creature controlled by the spell's controller")
    void targetMustBeControlledCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new EnvironmentalScientist());
        harness.setHand(player1, List.of(new GrowthCurve()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void targetMustBeCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new GrowthCurve()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses the counter total at resolution rather than at casting")
    void usesCounterTotalAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EnvironmentalScientist());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new GrowthCurve()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, target.getId());

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        harness.assertInGraveyard(player1, "Growth Curve");
    }

    @Test
    @DisplayName("Does not add or double counters if an opponent controls the target at resolution")
    void targetChangingControllerMakesSpellFizzle() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EnvironmentalScientist());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new GrowthCurve()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Growth Curve");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new GrowthCurve()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
