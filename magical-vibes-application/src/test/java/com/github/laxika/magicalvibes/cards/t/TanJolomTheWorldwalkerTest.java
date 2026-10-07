package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TanJolomTheWorldwalker.class, Forest.class, GrizzlyBears.class, Millstone.class, Ornithopter.class})
class TanJolomTheWorldwalkerTest extends BaseCardTest {

    @Test
    void animatesAnEligibleArtifactOrLandUntilEndOfTurn() {
        addCreatureReady(player1, new TanJolomTheWorldwalker());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent token = harness.addToBattlefieldAndReturn(player1, noncreatureArtifactToken());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId(), land.getId())
                .doesNotContain(opponentArtifact.getId(), creature.getId(), artifactCreature.getId(), token.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, artifact)).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.DOUBLE_TEAM)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void mayChooseNoPermanent() {
        addCreatureReady(player1, new TanJolomTheWorldwalker());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    @Test
    void animatedLandRetainsItsLandTypeAndAttacksWithoutTapping() {
        harness.addToBattlefield(player1, new TanJolomTheWorldwalker());
        Permanent land = addCreatureReady(player1, new Forest());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        resolveAllTriggers();

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1).allMatch(card -> card instanceof Forest);
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new TanJolomTheWorldwalker());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    @Test
    void canGrantDoubleTeamAgainAfterItWasPerpetuallyRemoved() {
        harness.addToBattlefield(player1, new TanJolomTheWorldwalker());
        Permanent land = addCreatureReady(player1, new Forest());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        harness.performUntapStep(player1);
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, land, Keyword.DOUBLE_TEAM)).isTrue();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2).allMatch(card -> card instanceof Forest);
    }

    @Test
    void resolvesWithoutAnEligiblePermanent() {
        harness.addToBattlefield(player1, new TanJolomTheWorldwalker());

        advanceToCombat(player1);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Card noncreatureArtifactToken() {
        Card card = new Card();
        card.setName("Artifact Token");
        card.setType(CardType.ARTIFACT);
        card.setToken(true);
        return card;
    }
}
