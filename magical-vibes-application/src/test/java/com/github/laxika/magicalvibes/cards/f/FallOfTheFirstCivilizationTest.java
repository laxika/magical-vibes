package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallOfTheFirstCivilization.class, DarksteelRelic.class, GrizzlyBears.class,
        Millstone.class, Forest.class})
class FallOfTheFirstCivilizationTest extends BaseCardTest {

    @Test
    void chapterIHasYouAndTargetOpponentDrawTwoCards() {
        Permanent saga = addSagaWithLore(0);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        triggerNextChapter();

        PendingInteraction.PermanentChoice target =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(target.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore + 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    void chapterIRejectsNonOpponentTarget() {
        addSagaWithLore(0);
        triggerNextChapter();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chapterIIExilesTargetArtifactAnOpponentControls() {
        Permanent saga = addSagaWithLore(1);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        triggerNextChapter();

        PendingInteraction.PermanentChoice target =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(target.validPermanentIds()).containsExactly(opponentArtifact.getId());
        assertThat(target.validPlayerIds()).containsExactly(player1.getId());
        harness.handlePermanentChosen(player1, opponentArtifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga, ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentArtifact);
        assertThat(gd.findExiledCard(opponentArtifact.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void chapterIIIDestroysOtherNonlandsAfterEachPlayerKeepsThree() {
        Permanent saga = addSagaWithLore(2);
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent thirdBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent opponentFirstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentSecondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentThirdBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent relic = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        Permanent player1Forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player2Forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        triggerNextChapter();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.maxCount()).isEqualTo(3);
        assertThat(player1Choice.validIds()).containsExactly(
                saga.getId(), firstBear.getId(), secondBear.getId(), thirdBear.getId(), millstone.getId());
        harness.handleMultiplePermanentsChosen(player1,
                List.of(saga.getId(), firstBear.getId(), secondBear.getId()));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.validIds()).containsExactly(
                opponentFirstBear.getId(), opponentSecondBear.getId(), opponentThirdBear.getId(), relic.getId());
        harness.handleMultiplePermanentsChosen(player2,
                List.of(opponentFirstBear.getId(), opponentSecondBear.getId(), opponentThirdBear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstBear, secondBear, player1Forest)
                .doesNotContain(saga, thirdBear, millstone);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(opponentFirstBear, opponentSecondBear, opponentThirdBear, relic, player2Forest);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .map(card -> card.getName()))
                .contains("Fall of the First Civilization", "Grizzly Bears", "Millstone");
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FallOfTheFirstCivilization());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
