package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaylayingPirates.class, GrizzlyBears.class, Millstone.class})
class WaylayingPiratesTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an opponent's creature and puts a stun counter on it when you control an artifact")
    void tapsAndStunsOpponentCreatureWithControlledArtifact() {
        harness.addToBattlefield(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWaylayingPirates(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target an opponent's noncreature artifact")
    void tapsAndStunsOpponentArtifact() {
        harness.addToBattlefield(player1, new Millstone());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        castWaylayingPirates(artifact);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when you do not control an artifact")
    void doesNothingWithoutControlledArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new WaylayingPirates(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("Cannot target a permanent you control")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new WaylayingPirates(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's artifact does not satisfy the trigger condition")
    void opponentArtifactDoesNotEnableTrigger() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        harness.castFromHand(player1, new WaylayingPirates(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(artifact.isTapped()).isFalse();
        assertThat(artifact.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("An already tapped target still receives a stun counter")
    void alreadyTappedTargetReceivesStunCounter() {
        harness.addToBattlefield(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();

        castWaylayingPirates(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The artifact condition is checked again when the trigger resolves")
    void losingOnlyArtifactBeforeResolutionPreventsBothEffects() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new WaylayingPirates(), "{3}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger still resolves after Waylaying Pirates leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        harness.addToBattlefield(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new WaylayingPirates(), "{3}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(gd.stack).hasSize(1);

        Permanent pirates = findPermanent(player1, "Waylaying Pirates");
        gd.playerBattlefields.get(player1.getId()).remove(pirates);
        gd.playerGraveyards.get(player1.getId()).add(pirates.getCard());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A target that leaves the battlefield receives neither effect")
    void targetLeavingBattlefieldPreventsBothEffects() {
        harness.addToBattlefield(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new WaylayingPirates(), "{3}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A stun counter prevents the next untap and is then removed")
    void stunCounterReplacesNextUntapOnly() {
        harness.addToBattlefield(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWaylayingPirates(creature);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    private void castWaylayingPirates(Permanent target) {
        harness.castFromHand(player1, new WaylayingPirates(), "{3}{U}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
