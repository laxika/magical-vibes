package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({DovinArchitectOfLaw.class, Forest.class, GrizzlyBears.class})
class DovinArchitectOfLawTest extends BaseCardTest {

    @Test
    @DisplayName("+1 gains 2 life and draws a card")
    void plusOneGainsLifeAndDraws() {
        Permanent dovin = addReadyDovin(3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-1 taps a creature and keeps it tapped through its next untap step")
    void minusOneTapsAndSkipsTargetUntap() {
        addReadyDovin(3);
        Permanent creature = addReadyPermanent(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();

        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isTrue();

        advanceToNextTurn(player2);
        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("-1 cannot target a noncreature permanent")
    void minusOneRejectsNoncreatureTarget() {
        Permanent dovin = addReadyDovin(3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, dovin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-9 taps an opponent's permanents and skips that player's next untap step")
    void minusNineTapsAndSkipsOpponentsUntap() {
        addReadyDovin(9);
        Permanent ownPermanent = addReadyPermanent(player1, new GrizzlyBears());
        Permanent opposingCreature = addReadyPermanent(player2, new GrizzlyBears());
        Permanent opposingLand = addReadyPermanent(player2, new Forest());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(ownPermanent.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(opposingLand.isTapped()).isTrue();

        advanceToNextTurn(player1);
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(opposingLand.isTapped()).isTrue();

        advanceToNextTurn(player2);
        advanceToNextTurn(player1);
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(opposingLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("-9 can target only an opponent")
    void minusNineRejectsControllerAsTarget() {
        addReadyDovin(9);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private Permanent addReadyDovin(int loyalty) {
        Permanent dovin = harness.addToBattlefieldAndReturn(player1, new DovinArchitectOfLaw());
        dovin.setCounterCount(CounterType.LOYALTY, loyalty);
        dovin.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return dovin;
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(currentActivePlayer == player1 ? player2 : player1, TurnStep.UPKEEP);
    }
}
