package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbzanFalconerTest extends BaseCardTest {

    @Test
    @DisplayName("Outlast puts a +1/+1 counter on Abzan Falconer and taps it")
    void outlastPutsCounterAndTaps() {
        Permanent falconer = addFalconerReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(falconer.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(falconer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Outlast cannot be activated outside sorcery speed")
    void outlastRequiresSorcerySpeed() {
        addFalconerReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("A creature you control with a +1/+1 counter has flying")
    void counteredOwnCreatureHasFlying() {
        Permanent falconer = addFalconerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        falconer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, falconer, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Creatures without a +1/+1 counter and opponents' creatures do not gain flying")
    void onlyCounteredOwnCreaturesHaveFlying() {
        addFalconerReady(player1);
        Permanent uncountered = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying is lost when the +1/+1 counter is removed")
    void flyingEndsWhenCounterIsRemoved() {
        addFalconerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    private Permanent addFalconerReady(Player player) {
        return addCreatureReady(player, new AbzanFalconer());
    }
}
