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

    @Test
    @DisplayName("Affects every eligible creature present when the spell resolves")
    void affectsCreaturesEnteringBeforeResolution() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new ArcboundWorker());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new ArcboundWorker());

        harness.castFromHand(player1, new MagneticFlux(), "{2}{U}");

        assertThat(first.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(second.hasKeyword(Keyword.FLYING)).isFalse();
        Permanent enteringBeforeResolution = harness.enterBattlefieldAndReturn(player1, new ArcboundWorker());
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(second.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(enteringBeforeResolution.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Resolves with no artifact creatures controlled by the caster")
    void resolvesWithoutEligibleCreatures() {
        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new CrazedGoblin());
        Permanent ownArtifact = harness.enterBattlefieldAndReturn(player1, new DarksteelCitadel());
        Permanent opponentCreature = harness.enterBattlefieldAndReturn(player2, new ArcboundWorker());

        castMagneticFlux();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Magnetic Flux");
        assertThat(ownCreature.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(ownArtifact.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.FLYING)).isFalse();
    }
}
