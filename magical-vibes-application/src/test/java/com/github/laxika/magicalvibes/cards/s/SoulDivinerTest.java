package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlastZone;
import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.m.ManaGeode;
import com.github.laxika.magicalvibes.cards.o.OathOfKaya;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulDiviner.class, ManaGeode.class, BlastZone.class, GideonBlackblade.class, OathOfKaya.class})
class SoulDivinerTest extends BaseCardTest {

    @Test
    @DisplayName("Removes a counter from a creature you control and draws a card")
    void removesCounterFromCreatureAndDraws() {
        Permanent diviner = addReadyDiviner();
        diviner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        activateDiviner();

        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Can remove a counter from an artifact you control")
    void removesCounterFromArtifact() {
        addReadyDiviner();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ManaGeode());
        artifact.setCounterCount(CounterType.CHARGE, 1);

        activateDiviner();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Can remove a counter from a land you control")
    void removesCounterFromLand() {
        addReadyDiviner();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new BlastZone());
        land.setCounterCount(CounterType.CHARGE, 1);

        activateDiviner();

        assertThat(land.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Can remove a counter from a planeswalker you control")
    void removesCounterFromPlaneswalker() {
        addReadyDiviner();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        planeswalker.setCounterCount(CounterType.LOYALTY, 1);

        activateDiviner();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("Cannot remove a counter from an enchantment")
    void rejectsEnchantment() {
        addReadyDiviner();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new OathOfKaya());
        enchantment.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(this::activateDiviner)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
        assertThat(enchantment.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counter removal and tapping are costs; drawing waits for resolution")
    void paysCostsBeforeDrawing() {
        Permanent diviner = addReadyDiviner();
        diviner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(diviner.isTapped()).isTrue();
        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Opponent's counters cannot pay the activation cost")
    void rejectsOpponentsCounters() {
        Permanent diviner = addReadyDiviner();
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SoulDiviner());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(this::activateDiviner).isInstanceOf(IllegalStateException.class);

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(diviner.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Soul Diviner cannot activate its tap ability")
    void rejectsSummoningSickness() {
        Permanent diviner = addReadyDiviner();
        diviner.setSummoningSick(true);
        diviner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(this::activateDiviner).isInstanceOf(IllegalStateException.class);

        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(diviner.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Controller chooses which eligible permanent pays the cost")
    void choosesCounterBearingPermanent() {
        Permanent diviner = addReadyDiviner();
        diviner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new BlastZone());
        land.setCounterCount(CounterType.CHARGE, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, land.getId());

        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Multiple counter types require a choice before a counter is removed")
    void choosesCounterTypeBeforePaying() {
        Permanent diviner = addReadyDiviner();
        diviner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        diviner.setCounterCount(CounterType.FINALITY, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(diviner.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can remove loyalty from a noncreature planeswalker on the opponent's turn")
    void activatesOnOpponentsTurnUsingPlaneswalker() {
        addReadyDiviner();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("A tapped Soul Diviner cannot activate again")
    void rejectsTappedSource() {
        Permanent diviner = addReadyDiviner();
        diviner.tap();
        diviner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(this::activateDiviner).isInstanceOf(IllegalStateException.class);

        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyDiviner() {
        return addCreatureReady(player1, new SoulDiviner());
    }

    private void activateDiviner() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
