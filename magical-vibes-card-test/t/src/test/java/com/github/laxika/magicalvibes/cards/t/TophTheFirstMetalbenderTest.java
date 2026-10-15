package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TophTheFirstMetalbender.class, Forest.class, GrizzlyBears.class, SolRing.class,
        TrustyBoomerang.class})
class TophTheFirstMetalbenderTest extends BaseCardTest {

    @Test
    void turnsYourNontokenArtifactsIntoLands() {
        harness.addToBattlefield(player1, new TophTheFirstMetalbender());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent ownNonArtifact = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent artifactToken = harness.addToBattlefieldAndReturn(player1, artifactToken());

        assertThat(gqs.isLand(gd, ownArtifact)).isTrue();
        assertThat(gqs.isLand(gd, ownNonArtifact)).isFalse();
        assertThat(gqs.isLand(gd, opponentArtifact)).isFalse();
        assertThat(gqs.isLand(gd, artifactToken)).isFalse();
        assertThat(gqs.isArtifact(ownArtifact)).isTrue();
    }

    @Test
    void earthbendsAChosenLandAtYourEndStep() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new TophTheFirstMetalbender());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private static Card artifactToken() {
        Card card = new Card();
        card.setName("Artifact Token");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("");
        card.setToken(true);
        return card;
    }

    @Test
    void earthbentArtifactRemainsALandCreatureAfterTophLeaves() {
        Permanent toph = harness.addToBattlefieldAndReturn(player1, new TophTheFirstMetalbender());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TrustyBoomerang());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new TrustyBoomerang());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, artifactToken());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(artifact.getId(), otherArtifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, toph));

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.isArtifact(artifact)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(2);
        assertThat(gqs.isLand(gd, otherArtifact)).isFalse();
        assertThat(gqs.isLand(gd, artifact)).isTrue();
    }

    @Test
    void doesNotEarthbendDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new TophTheFirstMetalbender());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void earthbentArtifactReturnsTappedAfterDyingWithoutToph() {
        Permanent artifact = earthbendArtifactAndRemoveToph();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, artifact));
        harness.passBothPriorities();

        assertReturnedArtifact();
    }

    @Test
    void earthbentArtifactReturnsTappedAfterExileWithoutToph() {
        Permanent artifact = earthbendArtifactAndRemoveToph();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, artifact));
        harness.passBothPriorities();

        assertReturnedArtifact();
    }

    private Permanent earthbendArtifactAndRemoveToph() {
        Permanent toph = harness.addToBattlefieldAndReturn(player1, new TophTheFirstMetalbender());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TrustyBoomerang());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, toph));
        return artifact;
    }

    private void assertReturnedArtifact() {
        Permanent returned = findPermanent(player1, "Trusty Boomerang");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isArtifact(returned)).isTrue();
        assertThat(gqs.isLand(gd, returned)).isFalse();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Trusty Boomerang");
        assertThat(gd.findExiledCard(returned.getCard().getId())).isNull();
    }
}
