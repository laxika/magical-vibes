package com.github.laxika.magicalvibes.cards.m;

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

class MerEkNightbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Outlast puts a +1/+1 counter on Mer-Ek Nightblade and taps it")
    void outlastPutsCounterAndTaps() {
        Permanent nightblade = addNightbladeReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(nightblade.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(nightblade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Outlast cannot be activated outside sorcery speed")
    void outlastRequiresSorcerySpeed() {
        addNightbladeReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("A creature you control with a +1/+1 counter has deathtouch")
    void counteredOwnCreatureHasDeathtouch() {
        Permanent nightblade = addNightbladeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        nightblade.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, nightblade, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Creatures without a +1/+1 counter and opponents' creatures do not gain deathtouch")
    void onlyCounteredOwnCreaturesHaveDeathtouch() {
        addNightbladeReady(player1);
        Permanent uncountered = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Deathtouch is lost when the +1/+1 counter is removed")
    void deathtouchEndsWhenCounterIsRemoved() {
        addNightbladeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent addNightbladeReady(Player player) {
        return addCreatureReady(player, new MerEkNightblade());
    }
}
