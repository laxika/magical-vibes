package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DramaticReversal.class, GrizzlyBears.class, Island.class, MindStone.class})
class DramaticReversalTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all nonland permanents you control")
    void untapsAllNonlandPermanentsYouControl() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        bear.tap();
        mindStone.tap();
        island.tap();
        opponentCreature.tap();

        harness.setHand(player1, List.of(new DramaticReversal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(bear.isTapped()).isFalse();
        assertThat(mindStone.isTapped()).isFalse();
        assertThat(island.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Uses the battlefield and tapped state at resolution")
    void untapsPermanentsThatEnterOrBecomeTappedBeforeResolution() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DramaticReversal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        bear.tap();
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        mindStone.tap();
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
        assertThat(mindStone.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Dramatic Reversal");
    }

    @Test
    @DisplayName("Resolves when the controller has no nonland permanents")
    void resolvesWithoutNonlandPermanents() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        island.tap();
        opponentArtifact.tap();
        harness.setHand(player1, List.of(new DramaticReversal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(island.isTapped()).isTrue();
        assertThat(opponentArtifact.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Dramatic Reversal");
    }
}
