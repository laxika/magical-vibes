package com.github.laxika.magicalvibes.cards.l;

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

class LongshotSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Outlast puts a +1/+1 counter on Longshot Squad and taps it")
    void outlastPutsCounterAndTaps() {
        Permanent squad = addSquadReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(squad.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(squad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Outlast cannot be activated outside sorcery speed")
    void outlastRequiresSorcerySpeed() {
        addSquadReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("A creature you control with a +1/+1 counter has reach")
    void counteredOwnCreatureHasReach() {
        Permanent squad = addSquadReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        squad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, squad, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Creatures without a +1/+1 counter and opponents' creatures do not gain reach")
    void onlyCounteredOwnCreaturesHaveReach() {
        addSquadReady(player1);
        Permanent uncountered = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Reach is lost when the +1/+1 counter is removed")
    void reachEndsWhenCounterIsRemoved() {
        addSquadReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    private Permanent addSquadReady(Player player) {
        return addCreatureReady(player, new LongshotSquad());
    }
}
