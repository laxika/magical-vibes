package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SeatOfTheSynod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LumengridSentinel.class, Atog.class, Ornithopter.class, SeatOfTheSynod.class})
class LumengridSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact entering under your control may tap a target permanent")
    void artifactEnteringUnderYourControlMayTapTargetPermanent() {
        harness.addToBattlefield(player1, new LumengridSentinel());
        var target = harness.addToBattlefieldAndReturn(player2, new Atog());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the trigger leaves the target untapped")
    void decliningTriggerLeavesTargetUntapped() {
        harness.addToBattlefield(player1, new LumengridSentinel());
        var target = harness.addToBattlefieldAndReturn(player2, new Atog());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A nonartifact permanent entering under your control does not trigger")
    void nonartifactEnteringUnderYourControlDoesNotTrigger() {
        harness.addToBattlefield(player1, new LumengridSentinel());
        var target = harness.addToBattlefieldAndReturn(player2, new Atog());
        harness.castFromHand(player1, new Atog(), "{1}{R}");
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control does not trigger")
    void artifactEnteringUnderOpponentsControlDoesNotTrigger() {
        harness.addToBattlefield(player1, new LumengridSentinel());
        var target = harness.addToBattlefieldAndReturn(player1, new Atog());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Ornithopter(), "{0}");
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The entering artifact can be the target of its trigger")
    void canTapEnteringArtifact() {
        harness.addToBattlefield(player1, new LumengridSentinel());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        resolveAllTriggers();
        var target = findPermanent(player1, "Ornithopter");

        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Playing an artifact land triggers and can tap that land")
    void artifactLandEnteringTriggersAndCanBeTapped() {
        harness.addToBattlefield(player1, new LumengridSentinel());
        harness.setHand(player1, List.of(new SeatOfTheSynod()));

        harness.playLand(player1, 0);
        resolveAllTriggers();
        var target = findPermanent(player1, "Seat of the Synod");
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
