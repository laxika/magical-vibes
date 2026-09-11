package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({OkibaReckonerRaid.class, NezumiRoadCaptain.class, MobilizerMech.class, GrizzlyBears.class})
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
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
