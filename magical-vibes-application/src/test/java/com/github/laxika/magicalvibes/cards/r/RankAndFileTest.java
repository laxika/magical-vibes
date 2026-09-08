package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RankAndFileTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives all green creatures -1/-1 and leaves non-green creatures alone")
    void etbWeakensAllGreenCreatures() {
        Permanent ownGreenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentGreenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent nonGreenCreature = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());

        harness.setHand(player1, List.of(new RankAndFile()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
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
        harness.addToBattlefieldAndReturn(player2, new ElvishMystic());

        harness.setHand(player1, List.of(new RankAndFile()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Elvish Mystic");
    }

    @Test
    @DisplayName("ETB -1/-1 wears off at end of turn")
    void etbWeakeningWearsOffAtEndOfTurn() {
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RankAndFile()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, greenCreature)).isEqualTo(2);
    }
}
