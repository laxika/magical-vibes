package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AuriokSalvagers;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfusedArrows.class, AuriokSalvagers.class})
class InfusedArrowsTest extends BaseCardTest {

    @Test
    void sunburstPutsOneChargeCounterForEachColorSpent() {
        harness.setHand(player1, List.of(new InfusedArrows()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent arrows = findPermanent(player1, "Infused Arrows");
        assertThat(arrows.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void sunburstCountsEachColorOnlyOnce() {
        harness.setHand(player1, List.of(new InfusedArrows()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent arrows = findPermanent(player1, "Infused Arrows");
        assertThat(arrows.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void removesChosenChargeCountersAndShrinksTargetCreatureUntilEndOfTurn() {
        Permanent arrows = harness.addToBattlefieldAndReturn(player1, new InfusedArrows());
        arrows.setCounterCount(CounterType.CHARGE, 2);
        Permanent salvagers = harness.addToBattlefieldAndReturn(player2, new AuriokSalvagers());

        harness.activateAbility(player1, 0, 1, salvagers.getId());
        harness.passBothPriorities();

        assertThat(arrows.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(arrows.isTapped()).isTrue();
        assertThat(salvagers.getEffectivePower()).isEqualTo(1);
        assertThat(salvagers.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(salvagers.getEffectivePower()).isEqualTo(2);
        assertThat(salvagers.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void canRemoveZeroChargeCounters() {
        Permanent arrows = harness.addToBattlefieldAndReturn(player1, new InfusedArrows());
        arrows.setCounterCount(CounterType.CHARGE, 1);
        Permanent salvagers = harness.addToBattlefieldAndReturn(player2, new AuriokSalvagers());

        harness.activateAbility(player1, 0, 0, salvagers.getId());
        harness.passBothPriorities();

        assertThat(arrows.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(arrows.isTapped()).isTrue();
        assertThat(salvagers.getEffectivePower()).isEqualTo(2);
        assertThat(salvagers.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void cannotRemoveMoreChargeCountersThanAvailable() {
        Permanent arrows = harness.addToBattlefieldAndReturn(player1, new InfusedArrows());
        arrows.setCounterCount(CounterType.CHARGE, 1);
        Permanent salvagers = harness.addToBattlefieldAndReturn(player2, new AuriokSalvagers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, salvagers.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
        assertThat(arrows.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(arrows.isTapped()).isFalse();
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent arrows = harness.addToBattlefieldAndReturn(player1, new InfusedArrows());
        arrows.setCounterCount(CounterType.CHARGE, 1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new InfusedArrows());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
