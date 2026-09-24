package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LumengridSentinel.class, Atog.class, Ornithopter.class})
class LumengridSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact entering under your control may tap a target permanent")
    void artifactEnteringUnderYourControlMayTapTargetPermanent() {
        harness.addToBattlefield(player1, new LumengridSentinel());
        var target = harness.addToBattlefieldAndReturn(player2, new Atog());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
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
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
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
        harness.setHand(player1, List.of(new Atog()));

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control does not trigger")
    void artifactEnteringUnderOpponentsControlDoesNotTrigger() {
        harness.addToBattlefield(player1, new LumengridSentinel());
        var target = harness.addToBattlefieldAndReturn(player1, new Atog());
        harness.setHand(player2, List.of(new Ornithopter()));

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
