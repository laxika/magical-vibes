package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReapingWillowTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two -1/-1 counters")
    void entersWithMinusOneMinusOneCounters() {
        Permanent willow = harness.addToBattlefieldAndReturn(player1, new ReapingWillow());

        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes two counters and returns a creature with mana value 3 or less")
    void removesCountersAndReanimatesCreature() {
        Permanent willow = harness.addToBattlefieldAndReturn(player1, new ReapingWillow());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 2);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can remove any counters, not only -1/-1 counters")
    void removesGenericCounters() {
        Permanent willow = harness.addToBattlefieldAndReturn(player1, new ReapingWillow());
        willow.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        willow.setCounterCount(CounterType.CHARGE, 2);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 2);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(willow.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rejects a creature card with mana value greater than 3")
    void rejectsHighManaValueCreature() {
        Permanent willow = harness.addToBattlefieldAndReturn(player1, new ReapingWillow());
        Card target = new AngelOfMercy();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 2);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
