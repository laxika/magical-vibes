package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LumengridSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact entering under your control may tap a target permanent")
    void artifactEnteringUnderYourControlMayTapTargetPermanent() {
        harness.addToBattlefield(player1, new LumengridSentinel());
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the trigger leaves the target untapped")
    void decliningTriggerLeavesTargetUntapped() {
        harness.addToBattlefield(player1, new LumengridSentinel());
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A nonartifact permanent entering under your control does not trigger")
    void nonartifactEnteringUnderYourControlDoesNotTrigger() {
        harness.addToBattlefield(player1, new LumengridSentinel());
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
