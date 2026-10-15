package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CoilingStalker;
import com.github.laxika.magicalvibes.cards.m.MobilizerMech;
import com.github.laxika.magicalvibes.cards.n.NezumiRoadCaptain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OkibaReckonerRaid.class, NezumiRoadCaptain.class, MobilizerMech.class, CoilingStalker.class})
class OkibaReckonerRaidTest extends BaseCardTest {

    @Test
    void chapterIDrainsEachOpponent() {
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void chapterIIDrainsEachOpponent() {
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void chapterIIITransformsAndGrantsMenaceToYourVehicles() {
        Permanent ownVehicle = harness.addToBattlefieldAndReturn(player1, new MobilizerMech());
        Permanent opponentsVehicle = harness.addToBattlefieldAndReturn(player2, new MobilizerMech());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CoilingStalker());
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent captain = findPermanent(player1, "Nezumi Road Captain");
        assertThat(captain).isNotNull();
        assertThat(captain.isTransformed()).isTrue();
        assertThat(gqs.hasKeyword(gd, ownVehicle, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentsVehicle, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    void castingSagaTriggersChapterIOnEntry() {
        harness.castFromHand(player1, new OkibaReckonerRaid(), "{B}");
        harness.passBothPriorities();

        Permanent saga = findPermanent(player1, "Okiba Reckoner Raid");
        assertThat(saga).isNotNull();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void chapterIIStillDrainsAfterSagaLeavesBattlefield() {
        Permanent saga = addSagaWithLore(1);
        advanceToNextChapter();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, saga));

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void chapterIIICannotReturnSagaThatAlreadyLeftBattlefield() {
        Permanent saga = addSagaWithLore(2);
        advanceToNextChapter();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, saga));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nezumi Road Captain");
        harness.assertInGraveyard(player1, "Okiba Reckoner Raid");
    }

    @Test
    void transformationCreatesNewPermanentAndVehicleGrantEndsWhenCaptainLeaves() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new MobilizerMech());
        Permanent saga = addSagaWithLore(2);
        saga.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        saga.setSummoningSick(false);
        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent captain = findPermanent(player1, "Nezumi Road Captain");
        assertThat(captain).isNotNull();
        assertThat(captain.getId()).isNotEqualTo(saga.getId());
        assertThat(captain.getCounterCount(CounterType.LORE)).isZero();
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(captain.isSummoningSick()).isTrue();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.MENACE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, captain));

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.MENACE)).isFalse();
        harness.assertInGraveyard(player1, "Okiba Reckoner Raid");
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OkibaReckonerRaid());
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
