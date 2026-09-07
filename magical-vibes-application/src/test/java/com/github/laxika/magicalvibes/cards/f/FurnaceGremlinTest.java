package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FurnaceGremlin.class, FlameJavelin.class})
class FurnaceGremlinTest extends BaseCardTest {

    @Test
    @DisplayName("Its ability gives it +1/+0 until end of turn")
    void activatedAbilityBoostsPower() {
        Permanent gremlin = harness.addToBattlefieldAndReturn(player1, new FurnaceGremlin());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gremlin)).isEqualTo(2);
    }

    @Test
    @DisplayName("When it dies, it incubates X where X is its power")
    void deathIncubatesItsPower() {
        Permanent gremlin = harness.addToBattlefieldAndReturn(player1, new FurnaceGremlin());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThat(gqs.getEffectivePower(gd, gremlin)).isEqualTo(2);
        harness.setHand(player2, java.util.List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.castInstant(player2, 0, gremlin.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Furnace Gremlin");
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
