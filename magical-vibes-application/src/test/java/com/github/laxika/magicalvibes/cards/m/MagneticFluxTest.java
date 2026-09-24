package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagneticFlux.class, ArcboundWorker.class, CrazedGoblin.class, DarksteelCitadel.class})
class MagneticFluxTest extends BaseCardTest {

    @Test
    @DisplayName("Gives flying to artifact creatures you control only")
    void givesFlyingToOwnArtifactCreaturesOnly() {
        Permanent ownArtifactCreature = harness.enterBattlefieldAndReturn(player1, new ArcboundWorker());
        Permanent ownNonartifactCreature = harness.enterBattlefieldAndReturn(player1, new CrazedGoblin());
        Permanent ownArtifactLand = harness.enterBattlefieldAndReturn(player1, new DarksteelCitadel());
        Permanent opponentArtifactCreature = harness.enterBattlefieldAndReturn(player2, new ArcboundWorker());

        castMagneticFlux();

        assertThat(ownArtifactCreature.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(ownNonartifactCreature.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(ownArtifactLand.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(opponentArtifactCreature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not affect artifact creatures that enter after resolution")
    void doesNotAffectArtifactCreaturesEnteringAfterResolution() {
        Permanent existingArtifactCreature = harness.enterBattlefieldAndReturn(player1, new ArcboundWorker());

        castMagneticFlux();

        Permanent laterArtifactCreature = harness.enterBattlefieldAndReturn(player1, new ArcboundWorker());

        assertThat(existingArtifactCreature.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(laterArtifactCreature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent ownArtifactCreature = harness.enterBattlefieldAndReturn(player1, new ArcboundWorker());

        castMagneticFlux();

        assertThat(ownArtifactCreature.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownArtifactCreature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    private void castMagneticFlux() {
        harness.castFromHand(player1, new MagneticFlux(), "{2}{U}");
        harness.passBothPriorities();
    }
}
