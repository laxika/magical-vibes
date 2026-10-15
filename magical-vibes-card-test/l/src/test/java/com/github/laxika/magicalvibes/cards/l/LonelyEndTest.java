package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KothOfTheHammer;
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

@CardUsed({LonelyEnd.class, GrizzlyBears.class, KothOfTheHammer.class})
class LonelyEndTest extends BaseCardTest {

    @Test
    void creatureModeGivesMinusThreeMinusThreeUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.startingPlayerId = player1.getId();

        cast(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(-3);
        assertThat(creature.getToughnessModifier()).isEqualTo(-3);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    void planeswalkerModeRemovesThreeLoyaltyCounters() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new KothOfTheHammer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);
        gd.startingPlayerId = player1.getId();

        cast(player1, 1, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertLife(player1, 20);
    }

    @Test
    void nonStartingPlayerGainsThreeLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.startingPlayerId = player1.getId();

        cast(player2, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(-3);
        harness.assertLife(player2, 23);
    }

    @Test
    void eachModeRejectsTheOtherModeTargetType() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new KothOfTheHammer());
        harness.setHand(player1, List.of(new LonelyEnd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(com.github.laxika.magicalvibes.model.Player caster, int mode, java.util.UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new LonelyEnd()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);

        harness.castModalInstant(caster, 0, mode, List.of(targetId));
        harness.passBothPriorities();
    }
}
