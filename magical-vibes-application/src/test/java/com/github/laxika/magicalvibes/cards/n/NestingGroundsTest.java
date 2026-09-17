package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NestingGrounds.class, GrizzlyBears.class, Forest.class})
class NestingGroundsTest extends BaseCardTest {

    @Test
    @DisplayName("Moves one counter from a permanent you control onto a second permanent")
    void movesCounterBetweenPermanents() {
        Permanent grounds = addReadyGrounds(player1);
        Permanent source = addPermanent(player1, new GrizzlyBears());
        Permanent destination = addPermanent(player2, new Forest());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        activateMoveAbility(source, destination);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(grounds.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Requires the first target permanent to be controlled by the ability controller")
    void rejectsFirstTargetNotControlled() {
        Permanent grounds = addReadyGrounds(player1);
        Permanent source = addPermanent(player2, new GrizzlyBears());
        Permanent destination = addPermanent(player1, new Forest());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareActivation(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(source.getId(), destination.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(grounds.isTapped()).isFalse();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be activated only at sorcery speed")
    void onlyActivatesAtSorcerySpeed() {
        Permanent grounds = addReadyGrounds(player1);
        Permanent source = addPermanent(player1, new GrizzlyBears());
        Permanent destination = addPermanent(player2, new Forest());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(source.getId(), destination.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(grounds.isTapped()).isFalse();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyGrounds(Player player) {
        Permanent grounds = new Permanent(new NestingGrounds());
        grounds.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(grounds);
        return grounds;
    }

    private Permanent addPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void activateMoveAbility(Permanent source, Permanent destination) {
        prepareActivation(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), destination.getId()));
        harness.passBothPriorities();
    }

    private void prepareActivation(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
