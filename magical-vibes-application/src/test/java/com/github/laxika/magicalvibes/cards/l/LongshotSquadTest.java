package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.HighlandGame;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LongshotSquad.class, HighlandGame.class})
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
        Permanent creature = addCreatureReady(player1, new HighlandGame());
        squad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, squad, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Creatures without a +1/+1 counter and opponents' creatures do not gain reach")
    void onlyCounteredOwnCreaturesHaveReach() {
        addSquadReady(player1);
        Permanent uncountered = addCreatureReady(player1, new HighlandGame());
        Permanent opponentCreature = addCreatureReady(player2, new HighlandGame());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Reach is lost when the +1/+1 counter is removed")
    void reachEndsWhenCounterIsRemoved() {
        addSquadReady(player1);
        Permanent creature = addCreatureReady(player1, new HighlandGame());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    @Test
    void outlastCannotBeActivatedWithSummoningSickness() {
        Permanent squad = addSquadReady(player1);
        squad.setSummoningSick(true);
        prepareOutlast();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(squad.isTapped()).isFalse();
        assertThat(squad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void outlastCannotBeActivatedWhileTapped() {
        Permanent squad = addSquadReady(player1);
        squad.tap();
        prepareOutlast();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(squad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void outlastCannotBeActivatedDuringOpponentsMainPhase() {
        Permanent squad = addSquadReady(player1);
        prepareOutlast();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(squad.isTapped()).isFalse();
        assertThat(squad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void outlastCannotBeActivatedWithAnAbilityOnTheStack() {
        addSquadReady(player1);
        Permanent secondSquad = addSquadReady(player1);
        prepareOutlast();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(secondSquad.isTapped()).isFalse();
        assertThat(secondSquad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
    }

    @Test
    void reachEndsWhenSquadLeavesTheBattlefield() {
        Permanent squad = addSquadReady(player1);
        Permanent creature = addCreatureReady(player1, new HighlandGame());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, squad));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    @Test
    void resolvingOutlastGrantsReachToSquad() {
        Permanent squad = addSquadReady(player1);
        prepareOutlast();
        assertThat(gqs.hasKeyword(gd, squad, Keyword.REACH)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(squad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, squad, Keyword.REACH)).isFalse();
        harness.passBothPriorities();

        assertThat(squad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, squad, Keyword.REACH)).isTrue();
    }

    private void prepareOutlast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private Permanent addSquadReady(Player player) {
        return addCreatureReady(player, new LongshotSquad());
    }
}
