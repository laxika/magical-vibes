package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SeaGateLoremaster;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajuruWarcaller.class, GrizzlyBears.class, SeaGateLoremaster.class})
class TajuruWarcallerTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry gives your creatures +2/+2 until end of turn")
    void ownAllyEntryBoostsYourCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new TajuruWarcaller());
        harness.passBothPriorities();

        Permanent warcaller = findPermanent(player1, "Tajuru Warcaller");
        assertThat(gqs.getEffectivePower(gd, warcaller)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warcaller)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Another Ally entry gives your creatures +2/+2 until end of turn")
    void anotherAllyEntryBoostsYourCreatures() {
        Permanent warcaller = addCreatureReady(player1, new TajuruWarcaller());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new SeaGateLoremaster());
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Sea Gate Loremaster");
        assertThat(gqs.getEffectivePower(gd, warcaller)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warcaller)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(5);
    }

    @Test
    @DisplayName("A non-Ally or opponent Ally entry does not trigger it")
    void nonAllyAndOpponentAllyEntriesDoNotTrigger() {
        Permanent warcaller = addCreatureReady(player1, new TajuruWarcaller());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, warcaller)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warcaller)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player2, new SeaGateLoremaster());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, warcaller)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warcaller)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new TajuruWarcaller());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
