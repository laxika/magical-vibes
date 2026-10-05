package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({OriginOfTheHulk.class, GrizzlyBears.class})
class OriginOfTheHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a 1/1 green and white Citizen token")
    void chapterICreatesCitizenToken() {
        addAndResolveSaga();

        Permanent citizen = findPermanent(player1, "Citizen");
        assertThat(citizen).isNotNull();
        assertThat(citizen.getCard().getPower()).isEqualTo(1);
        assertThat(citizen.getCard().getToughness()).isEqualTo(1);
        assertThat(citizen.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(citizen.getCard().getSubtypes()).contains(CardSubtype.CITIZEN);
        assertThat(citizen.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Chapter II puts two +1/+1 counters on a creature you control")
    void chapterIIPutsTwoCountersOnControlledCreature() {
        Permanent saga = addSagaWithLore(1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III gives a controlled creature +3/+3 and trample until end of turn")
    void chapterIIIBoostsAndGrantsTrampleUntilEndOfTurn() {
        addSagaWithLore(2);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Chapter II only targets creatures you control")
    void chaptersOnlyTargetOwnCreatures() {
        addSagaWithLore(1);
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownBears.getId())
                .doesNotContain(opponentBears.getId());
    }

    @Test
    void chapterIIRequiresACreatureTargetWhenOneIsAvailable() {
        addAndResolveSaga();
        Permanent citizen = findPermanent(player1, "Citizen");

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(citizen.getId());
    }

    @Test
    void chapterIIIRequiresAnOwnCreatureTargetAndExcludesOpposingCreatures() {
        addAndResolveSaga();
        Permanent citizen = findPermanent(player1, "Citizen");
        harness.addToBattlefield(player2, new GrizzlyBears());
        findPermanent(player1, "Origin of the Hulk").setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(citizen.getId());
    }

    @Test
    void citizenKeepsChapterIICountersAfterChapterIIIBoostExpires() {
        addAndResolveSaga();
        Permanent saga = findPermanent(player1, "Origin of the Hulk");
        Permanent citizen = findPermanent(player1, "Citizen");

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, citizen.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, citizen)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, citizen)).isEqualTo(3);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, citizen.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, citizen)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, citizen)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, citizen, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(citizen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, citizen)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, citizen)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, citizen, Keyword.TRAMPLE)).isFalse();
    }

    private void addAndResolveSaga() {
        harness.castFromHand(player1, new OriginOfTheHulk(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OriginOfTheHulk());
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
