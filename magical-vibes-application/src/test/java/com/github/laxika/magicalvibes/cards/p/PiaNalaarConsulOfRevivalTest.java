package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiaNalaarConsulOfRevival.class, Forest.class, GrizzlyBears.class})
class PiaNalaarConsulOfRevivalTest extends BaseCardTest {

    @Test
    void createsAHastyThopterWhenPlayingALandFromExile() {
        addCreatureReady(player1, new PiaNalaarConsulOfRevival());
        Forest forest = new Forest();
        gd.addToExile(player1.getId(), forest);
        gd.exilePlayPermissions.put(forest.getId(), player1.getId());

        prepareMainPhase();
        gs.playCardFromExile(gd, player1, forest.getId(), null, null);
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.HASTE)).isTrue();
    }

    @Test
    void createsAHastyThopterWhenCastingASpellFromExile() {
        addCreatureReady(player1, new PiaNalaarConsulOfRevival());
        GrizzlyBears bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), bears);
        gd.exilePlayPermissions.put(bears.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        prepareMainPhase();
        gs.playCardFromExile(gd, player1, bears.getId(), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.HASTE)).isTrue();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
