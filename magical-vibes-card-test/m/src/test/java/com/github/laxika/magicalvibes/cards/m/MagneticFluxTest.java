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

        assertThat(gqs.hasKeyword(gd, ownArtifactCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonartifactCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownArtifactLand, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentArtifactCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not affect artifact creatures that enter after resolution")
    void doesNotAffectArtifactCreaturesEnteringAfterResolution() {
        Permanent existingArtifactCreature = harness.enterBattlefieldAndReturn(player1, new ArcboundWorker());

        castMagneticFlux();

        Permanent laterArtifactCreature = harness.enterBattlefieldAndReturn(player1, new ArcboundWorker());

        assertThat(gqs.hasKeyword(gd, existingArtifactCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterArtifactCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent ownArtifactCreature = harness.enterBattlefieldAndReturn(player1, new ArcboundWorker());

        castMagneticFlux();

        assertThat(gqs.hasKeyword(gd, ownArtifactCreature, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownArtifactCreature, Keyword.FLYING)).isFalse();
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

        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isFalse();
        Permanent enteringBeforeResolution = harness.enterBattlefieldAndReturn(player1, new ArcboundWorker());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, enteringBeforeResolution, Keyword.FLYING)).isTrue();
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
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownArtifact, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
    }
}
