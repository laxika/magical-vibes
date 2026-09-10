package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.j.JhoirasToolbox;
import com.github.laxika.magicalvibes.cards.l.LoneWolf;
import com.github.laxika.magicalvibes.cards.w.WeatherseedElf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RankAndFile.class, JhoirasToolbox.class, LoneWolf.class, WeatherseedElf.class})
class RankAndFileTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives all green creatures -1/-1 and leaves non-green creatures alone")
    void etbWeakensAllGreenCreatures() {
        Permanent ownGreenCreature = harness.addToBattlefieldAndReturn(player1, new LoneWolf());
        Permanent opponentGreenCreature = harness.addToBattlefieldAndReturn(player2, new LoneWolf());
        Permanent nonGreenCreature = harness.addToBattlefieldAndReturn(player1, new JhoirasToolbox());

        harness.castFromHand(player1, new RankAndFile(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownGreenCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownGreenCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentGreenCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentGreenCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, nonGreenCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonGreenCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB -1/-1 kills a green 1/1")
    void etbKillsGreenOneToughnessCreature() {
        harness.addToBattlefieldAndReturn(player2, new WeatherseedElf());

        harness.castFromHand(player1, new RankAndFile(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Weatherseed Elf");
    }

    @Test
    @DisplayName("ETB -1/-1 wears off at end of turn")
    void etbWeakeningWearsOffAtEndOfTurn() {
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player2, new LoneWolf());

        harness.castFromHand(player1, new RankAndFile(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, greenCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB does not affect green creatures that enter after the trigger resolves")
    void etbDoesNotAffectGreenCreaturesEnteringLater() {
        Permanent initialGreenCreature = harness.addToBattlefieldAndReturn(player2, new LoneWolf());

        harness.castFromHand(player1, new RankAndFile(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent laterGreenCreature = harness.enterBattlefieldAndReturn(player2, new LoneWolf());

        assertThat(gqs.getEffectivePower(gd, initialGreenCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, initialGreenCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, laterGreenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterGreenCreature)).isEqualTo(2);
    }
}
