package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverlordOfTheHauntwoods.class})
class OverlordOfTheHauntwoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a tapped Everywhere land token")
    void enteringCreatesEverywhere() {
        harness.setHand(player1, List.of(new OverlordOfTheHauntwoods()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent everywhere = findPermanent(player1, "Everywhere");
        assertThat(everywhere.isTapped()).isTrue();
        assertThat(everywhere.getCard().getType()).isEqualTo(CardType.LAND);
        assertThat(everywhere.getCard().getColor()).isNull();
        assertThat(everywhere.getCard().getSubtypes()).containsExactlyInAnyOrder(
                CardSubtype.PLAINS,
                CardSubtype.ISLAND,
                CardSubtype.SWAMP,
                CardSubtype.MOUNTAIN,
                CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Casting with impending enters with four time counters and is not a creature")
    void impendingCastEntersWithCountersAndIsNotCreature() {
        Permanent overlord = castWithImpending();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    @Test
    @DisplayName("Removing the last impending counter makes it a creature")
    void lastCounterMakesItCreature() {
        Permanent overlord = castWithImpending();
        overlord.setCounterCount(CounterType.TIME, 1);

        advanceToOwnEndStep();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gqs.isCreature(gd, overlord)).isTrue();
    }

    @Test
    @DisplayName("Attacking creates another Everywhere land token")
    void attackingCreatesEverywhere() {
        Permanent overlord = addCreatureReady(player1, new OverlordOfTheHauntwoods());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Everywhere")).hasSize(1);
        assertThat(overlord.isAttackedThisTurn()).isTrue();
    }

    private Permanent castWithImpending() {
        harness.setHand(player1, List.of(new OverlordOfTheHauntwoods()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Overlord of the Hauntwoods");
    }

    private void advanceToOwnEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
