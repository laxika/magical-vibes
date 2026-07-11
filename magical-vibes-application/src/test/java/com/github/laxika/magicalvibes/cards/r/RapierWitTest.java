package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RapierWitTest extends BaseCardTest {

    @Test
    @DisplayName("Rapier Wit needs a target and has three effects")
    void hasCorrectStructure() {
        RapierWit card = new RapierWit();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(3);
    }

    @Test
    @DisplayName("On your turn: taps the target, adds a stun counter, and draws a card")
    void tapsStunsAndDrawsOnYourTurn() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.getGameData().playerDecks.get(player1.getId()).add(new GrizzlyBears());
        harness.setHand(player1, List.of(new RapierWit()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Stun counter keeps the creature tapped through the next untap and is consumed")
    void stunCounterReplacesUntap() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RapierWit()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);

        // Untapping consumes the stun counter instead of untapping the creature.
        bear.untap();
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(0);

        // Next untap actually untaps it.
        bear.untap();
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("On opponent's turn: taps and draws but adds no stun counter")
    void noStunOnOpponentsTurn() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new RapierWit()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new GrizzlyBears()); // valid target so the spell is playable
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new RapierWit()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
