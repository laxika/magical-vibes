package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetalFatigueTest extends BaseCardTest {

    @Test
    @DisplayName("Taps every artifact on every battlefield")
    void tapsAllArtifacts() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new JayemdaeTome());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());

        castAndResolveMetalFatigue();

        assertThat(ownArtifact.isTapped()).isTrue();
        assertThat(opposingArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap non-artifact permanents")
    void doesNotTapNonArtifacts() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolveMetalFatigue();

        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
    }

    private void castAndResolveMetalFatigue() {
        harness.setHand(player1, List.of(new MetalFatigue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
