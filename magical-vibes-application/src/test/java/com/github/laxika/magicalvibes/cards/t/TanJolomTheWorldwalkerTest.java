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
        addReadyTanJolom(player1);
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

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void mayChooseNoPermanent() {
        addReadyTanJolom(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    private Permanent addReadyTanJolom(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TanJolomTheWorldwalker());
        permanent.setSummoningSick(false);
        return permanent;
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
