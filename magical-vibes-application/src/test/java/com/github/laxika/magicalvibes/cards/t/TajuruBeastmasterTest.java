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

@CardUsed({TajuruBeastmaster.class, GrizzlyBears.class, SeaGateLoremaster.class})
class TajuruBeastmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry gives your creatures +1/+1 until end of turn")
    void ownAllyEntryBoostsYourCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new TajuruBeastmaster());
        harness.passBothPriorities();

        Permanent beastmaster = findPermanent(player1, "Tajuru Beastmaster");
        assertThat(gqs.getEffectivePower(gd, beastmaster)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, beastmaster)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Another Ally entry gives your creatures +1/+1 until end of turn")
    void anotherAllyEntryBoostsYourCreatures() {
        Permanent beastmaster = addCreatureReady(player1, new TajuruBeastmaster());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new SeaGateLoremaster());
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Sea Gate Loremaster");
        assertThat(gqs.getEffectivePower(gd, beastmaster)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, beastmaster)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(4);
    }

    @Test
    @DisplayName("A non-Ally or opponent Ally entry does not trigger it")
    void nonAllyAndOpponentAllyEntriesDoNotTrigger() {
        Permanent beastmaster = addCreatureReady(player1, new TajuruBeastmaster());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, beastmaster)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beastmaster)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player2, new SeaGateLoremaster());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, beastmaster)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beastmaster)).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new TajuruBeastmaster());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
