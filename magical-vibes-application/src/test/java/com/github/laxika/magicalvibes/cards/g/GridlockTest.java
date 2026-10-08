package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gridlock.class, GrizzlyBears.class, RodOfRuin.class, Forest.class})
class GridlockTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 taps two target nonland permanents")
    void tapsTwoTargetNonlandPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new Gridlock()));
        harness.addMana(player1, ManaColor.BLUE, 3); // X=2: {2}{U} = 3

        harness.castInstantForX(player1, 0, 2, List.of(creature.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("X=0 taps no permanents")
    void xZeroDoesNothing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Gridlock()));
        harness.addMana(player1, ManaColor.BLUE, 1); // X=0: {0}{U} = 1

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Gridlock()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Cannot choose fewer than X targets")
    void cannotChooseFewerThanXTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Gridlock()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Positive X requires targets")
    void positiveXRequiresTargets() {
        harness.setHand(player1, List.of(new Gridlock()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose more than X targets")
    void cannotChooseMoreThanXTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new Gridlock()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1,
                List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same permanent twice")
    void cannotChooseDuplicateTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Gridlock()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target your own permanents and already tapped permanents")
    void canTargetOwnAndAlreadyTappedPermanents() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent tappedArtifact = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        Permanent unchosenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedArtifact.setTapped(true);
        harness.setHand(player1, List.of(new Gridlock()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstantForX(player1, 0, 2, List.of(ownCreature.getId(), tappedArtifact.getId()));
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(tappedArtifact.isTapped()).isTrue();
        assertThat(unchosenCreature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Gridlock");
    }

    @Test
    @DisplayName("Still taps the remaining target when another target leaves the battlefield")
    void resolvesWithOneRemainingTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new Gridlock()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstantForX(player1, 0, 2, List.of(creature.getId(), artifact.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Gridlock");
    }
}
