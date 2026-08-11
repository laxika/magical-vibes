package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MagneticFluxTest extends BaseCardTest {

    @Test
    @DisplayName("Gives flying to artifact creatures you control only")
    void givesFlyingToOwnArtifactCreaturesOnly() {
        Permanent ownArtifactCreature = harness.addToBattlefieldAndReturn(player1, new DarksteelMyr());
        Permanent ownNonartifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifactCreature = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());

        castMagneticFlux();

        assertThat(ownArtifactCreature.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(ownNonartifactCreature.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(opponentArtifactCreature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent ownArtifactCreature = harness.addToBattlefieldAndReturn(player1, new DarksteelMyr());

        castMagneticFlux();

        assertThat(ownArtifactCreature.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownArtifactCreature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    private void castMagneticFlux() {
        harness.setHand(player1, List.of(new MagneticFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
