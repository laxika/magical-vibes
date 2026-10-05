package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.ArcaneSignet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OakaTravelingMerchant.class, GrizzlyBears.class, Forest.class, ArcaneSignet.class})
class OakaTravelingMerchantTest extends BaseCardTest {

    @Test
    @DisplayName("Removes a counter from a nonland permanent and draws a card")
    void removesCounterFromNonlandPermanentAndDraws() {
        Permanent oaka = addReadyOaka();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        activateOaka(oaka);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Cannot remove a counter from a land")
    void rejectsLand() {
        Permanent oaka = addReadyOaka();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> activateOaka(oaka))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
        assertThat(land.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can remove its own counter, paying costs before drawing")
    void removesOwnCounterAsCost() {
        Permanent oaka = addReadyOaka();
        oaka.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(oaka.isTapped()).isTrue();
        assertThat(oaka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Can remove a charge counter from a noncreature artifact")
    void removesArtifactCounter() {
        Permanent oaka = addReadyOaka();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ArcaneSignet());
        artifact.setCounterCount(CounterType.CHARGE, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        activateOaka(oaka);

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Cannot pay with a counter on an opponent's permanent")
    void rejectsOpponentsCounters() {
        Permanent oaka = addReadyOaka();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ArcaneSignet());
        artifact.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> activateOaka(oaka)).isInstanceOf(IllegalStateException.class);
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(oaka.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without any removable counters")
    void rejectsMissingCounters() {
        Permanent oaka = addReadyOaka();

        assertThatThrownBy(() -> activateOaka(oaka)).isInstanceOf(IllegalStateException.class);
        assertThat(oaka.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick or tapped")
    void requiresUsableTapCost() {
        Permanent oaka = addReadyOaka();
        oaka.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        oaka.setSummoningSick(true);

        assertThatThrownBy(() -> activateOaka(oaka)).isInstanceOf(IllegalStateException.class);
        assertThat(oaka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        oaka.setSummoningSick(false);
        oaka.setTapped(true);

        assertThatThrownBy(() -> activateOaka(oaka)).isInstanceOf(IllegalStateException.class);
        assertThat(oaka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller chooses the permanent when multiple permanents can pay")
    void choosesCounterBearingPermanent() {
        Permanent oaka = addReadyOaka();
        oaka.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ArcaneSignet());
        artifact.setCounterCount(CounterType.CHARGE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(oaka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Controller chooses which kind of counter to remove")
    void doesNotAutomaticallyChooseCounterType() {
        Permanent oaka = addReadyOaka();
        oaka.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        oaka.setCounterCount(CounterType.FLYING, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(oaka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(oaka.getCounterCount(CounterType.FLYING)).isEqualTo(1);
    }

    private Permanent addReadyOaka() {
        return addCreatureReady(player1, new OakaTravelingMerchant());
    }

    private void activateOaka(Permanent oaka) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oaka);
        harness.activateAbility(player1, permanentIndex, 0, null, null);
        harness.passBothPriorities();
    }
}
