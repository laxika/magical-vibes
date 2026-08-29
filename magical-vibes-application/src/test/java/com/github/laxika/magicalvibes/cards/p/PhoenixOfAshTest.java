package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({PhoenixOfAsh.class, GrizzlyBears.class})
class PhoenixOfAshTest extends BaseCardTest {

    @Test
    void castFromHandEntersWithoutCounter() {
        harness.setHand(player1, List.of(new PhoenixOfAsh()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Phoenix of Ash")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void escapingExilesThreeOtherCardsAndAddsCounter() {
        PhoenixOfAsh phoenix = new PhoenixOfAsh();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(phoenix, first, second, third));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent escapedPhoenix = findPermanent(player1, "Phoenix of Ash");
        assertThat(escapedPhoenix.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(escapedPhoenix.getEffectivePower()).isEqualTo(3);
        assertThat(escapedPhoenix.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void escapeRequiresThreeOtherCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new PhoenixOfAsh(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent phoenix = addCreatureReady(player1, new PhoenixOfAsh());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(phoenix.getPowerModifier()).isEqualTo(2);
        assertThat(phoenix.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(phoenix.getPowerModifier()).isZero();
        assertThat(phoenix.getToughnessModifier()).isZero();
    }
}
