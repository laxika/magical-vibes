package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgeOfUltron.class, GrizzlyBears.class, Ornithopter.class})
class AgeOfUltronTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I destroys up to one nonartifact creature per opponent")
    void chapterIDestroysNonartifactCreatureAndSkipsArtifacts() {
        Permanent saga = addSagaWithLore(0);
        Permanent nonartifactCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(nonartifactCreature.getId());
        assertThat(choice.validIds()).doesNotContain(artifactCreature.getId());

        harness.handlePermanentChosen(player1, nonartifactCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(nonartifactCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifactCreature);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II creates one Robot Villain artifact creature per opponent under your control")
    void chapterIICreatesRobotVillainForEachOpponent() {
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Robot")).hasSize(1);
        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(robot.getCard().getSubtypes()).containsExactlyInAnyOrder(
                CardSubtype.ROBOT, CardSubtype.VILLAIN);
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(2);
        assertThat(findPermanents(player2, "Robot")).isEmpty();
    }

    @Test
    @DisplayName("Chapter III counters and grants deathtouch to your artifact creatures")
    void chapterIIIBoostsControlledArtifactCreaturesOnly() {
        addSagaWithLore(2);
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent nonartifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(artifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(nonartifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, nonartifactCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(opponentArtifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, opponentArtifactCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AgeOfUltron());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
