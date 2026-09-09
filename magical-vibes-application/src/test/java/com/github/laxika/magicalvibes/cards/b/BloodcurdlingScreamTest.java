package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodcurdlingScream.class, BearCub.class, Forest.class})
class BloodcurdlingScreamTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives target creature +X/+0")
    void resolvesAndBoostsPowerOnly() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BearCub());
        harness.setHand(player1, List.of(new BloodcurdlingScream()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 3, bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BearCub());
        harness.setHand(player1, List.of(new BloodcurdlingScream()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 2, bear.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentsCreature() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.setHand(player1, List.of(new BloodcurdlingScream()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 2, opponentBear.getId());

        assertThat(opponentBear.getPowerModifier()).isEqualTo(2);
        assertThat(opponentBear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        // A creature must exist so the spell has a legal target and is castable (CR 601.2c);
        // targeting the noncreature is then rejected by the spell's target-type validation.
        harness.addToBattlefield(player1, new BearCub());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BloodcurdlingScream()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
