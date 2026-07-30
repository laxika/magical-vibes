package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvernightShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives Evernight Shade +1/+1 until end of turn")
    void abilityBoostsSelf() {
        Permanent shade = addCreatureReady(player1, new EvernightShade());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(3);
        assertThat(shade.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off during cleanup")
    void boostWearsOffAtEndOfTurn() {
        Permanent shade = addCreatureReady(player1, new EvernightShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(1);
        assertThat(shade.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Undying returns Evernight Shade with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new EvernightShade());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, shade.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Evernight Shade");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.getEffectivePower()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Evernight Shade");
    }

    @Test
    @DisplayName("Undying does not return Evernight Shade when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new EvernightShade());
        shade.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, shade.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Evernight Shade");
    }
}
