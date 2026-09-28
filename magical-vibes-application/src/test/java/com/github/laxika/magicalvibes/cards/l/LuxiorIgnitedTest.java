package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LuxiorIgnited.class, GrizzlyBears.class})
class LuxiorIgnitedTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusOnePlusOneForEachCounterOnLuxior() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent luxior = addLuxior(player1, 1);
        luxior.setCounterCount(CounterType.CHARGE, 2);
        luxior.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void plusOneAttachesToUpToOneCreatureYouControl() {
        Permanent luxior = addLuxior(player1, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(luxior.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(luxior.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void plusOneCanResolveWithoutTarget() {
        Permanent luxior = addLuxior(player1, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(luxior.getAttachedTo()).isNull();
        assertThat(luxior.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void plusOneCannotTargetAnOpponentsCreature() {
        Permanent luxior = addLuxior(player1, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(luxior.getAttachedTo()).isNull();
    }

    @Test
    void minusTwoBoostsEquippedCreatureAndGrantsDoubleStrikeUntilEndOfTurn() {
        Permanent luxior = addLuxior(player1, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        luxior.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(luxior.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent addLuxior(Player player, int loyalty) {
        Permanent luxior = harness.addToBattlefieldAndReturn(player, new LuxiorIgnited());
        luxior.setCounterCount(CounterType.LOYALTY, loyalty);
        luxior.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return luxior;
    }
}
