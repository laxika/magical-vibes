package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrapplingKraken.class, BearCub.class, Forest.class})
class GrapplingKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall taps and stuns a target creature an opponent controls")
    void landfallTapsAndStunsOpponentCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.addToBattlefield(player1, new GrapplingKraken());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall does not trigger for an opponent's land")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new GrapplingKraken());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Landfall cannot target a creature controlled by Grappling Kraken's controller")
    void cannotTargetOwnCreature() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new BearCub());
        harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.addToBattlefield(player1, new GrapplingKraken());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds())
                .doesNotContain(ownBear.getId());
    }

    @Test
    @DisplayName("An already tapped creature still receives a stun counter")
    void alreadyTappedCreatureReceivesStunCounter() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BearCub());
        bear.tap();
        harness.addToBattlefield(player1, new GrapplingKraken());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each entering land adds a stun counter and each untap removes only one")
    void repeatedLandfallAccumulatesStunCounters() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.addToBattlefield(player1, new GrapplingKraken());

        for (int i = 0; i < 2; i++) {
            harness.enterBattlefieldAndReturn(player1, new Forest());
            harness.handlePermanentChosen(player1, bear.getId());
            harness.passBothPriorities();
        }

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(2);
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Landfall resolves even if Grappling Kraken leaves the battlefield")
    void landfallResolvesAfterSourceLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BearCub());
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new GrapplingKraken());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(kraken);
        gd.playerGraveyards.get(player1.getId()).add(kraken.getCard());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall does nothing when the chosen creature is no longer an opponent's")
    void targetBecomingControlledByControllerIsIllegal() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.addToBattlefield(player1, new GrapplingKraken());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bear.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bear);
        gd.playerBattlefields.get(player1.getId()).add(bear);
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("Landfall with no opposing creatures does not leave an unresolved choice")
    void noLegalTargets() {
        harness.addToBattlefield(player1, new GrapplingKraken());
        harness.addToBattlefield(player1, new BearCub());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
