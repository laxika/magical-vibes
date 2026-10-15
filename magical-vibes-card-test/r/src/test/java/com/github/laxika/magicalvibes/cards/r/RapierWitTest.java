package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RapierWit.class, GrizzlyBears.class, FountainOfYouth.class})
class RapierWitTest extends BaseCardTest {

    @Test
    @DisplayName("On your turn: taps the target, adds a stun counter, and draws a card")
    void tapsStunsAndDrawsOnYourTurn() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RapierWit()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

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

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);

        // Untapping consumes the stun counter instead of untapping the creature.
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(0);

        // Next untap actually untaps it.
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("On opponent's turn: taps and draws but adds no stun counter")
    void noStunOnOpponentsTurn() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RapierWit()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(0);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
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

    @Test
    @DisplayName("An already tapped creature still gets a stun counter and you draw")
    void alreadyTappedCreatureStillGetsStunned() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RapierWit()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("If the only target leaves before resolution, you do not draw")
    void removedTargetPreventsDraw() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        GrizzlyBears drawCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawCard));
        harness.setHand(player1, List.of(new RapierWit()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bear.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bear);
        harness.setGraveyard(player2, List.of(bear.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawCard);
        harness.assertInGraveyard(player1, "Rapier Wit");
    }
}
