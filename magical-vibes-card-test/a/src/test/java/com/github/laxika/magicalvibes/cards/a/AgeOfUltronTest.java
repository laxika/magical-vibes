package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
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

    @Test
    @DisplayName("Chapter I may choose no target even when an opponent has a legal creature")
    void chapterICanChooseZeroTargets() {
        addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Chapter I cannot target a creature you control")
    void chapterIExcludesControlledCreatures() {
        addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opposingCreature.getId()).doesNotContain(ownCreature.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    @DisplayName("Chapter I chooses at most one creature controlled by the same opponent")
    void chapterILeavesOtherCreaturesOfTheSameOpponentAlone() {
        addSagaWithLore(0);
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unchosen).doesNotContain(chosen);
    }

    @Test
    @DisplayName("Chapter III deathtouch expires, its counter remains, and later creatures are unaffected")
    void chapterIIIDeathTouchExpiresButCounterRemains() {
        addSagaWithLore(2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        harness.assertInGraveyard(player1, "Age of Ultron");
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(laterCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed(MycosynthLattice.class)
    @DisplayName("Chapter I does not destroy a target that becomes an artifact before resolution")
    void chapterIRechecksNonartifactRestrictionOnResolution() {
        addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Chapter I resolves with no targets when the opponent only has artifact creatures")
    void chapterIHandlesNoLegalTargets() {
        addSagaWithLore(0);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        advanceToNextChapter();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }

    @Test
    @CardUsed({Opalescence.class, MycosynthLattice.class})
    @DisplayName("Chapter III includes the animated Saga after a lore counter is removed in response")
    void chapterIIIGrantsDeathtouchToAnimatedSagaItself() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new MycosynthLattice());
        Permanent saga = addSagaWithLore(2);

        advanceToNextChapter();
        saga.setCounterCount(CounterType.LORE, 2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        assertThat(saga.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, saga, Keyword.DEATHTOUCH)).isTrue();
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
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }
}
