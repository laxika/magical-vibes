package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerrorOfMountVelus.class, SavannahLions.class})
class TerrorOfMountVelusTest extends BaseCardTest {

    @Test
    void enteringGrantsDoubleStrikeToOwnCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new SavannahLions());

        castTerrorOfMountVelus();

        Permanent terror = findPermanent(player1, "Terror of Mount Velus");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, terror, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void grantedDoubleStrikeExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SavannahLions());

        castTerrorOfMountVelus();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void creaturesEnteringAfterTriggerResolvesDoNotGainDoubleStrike() {
        castTerrorOfMountVelus();

        Permanent lateCreature = harness.enterBattlefieldAndReturn(player1, new SavannahLions());

        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void creaturesEnteringBeforeTriggerResolvesGainDoubleStrike() {
        harness.castFromHand(player1, new TerrorOfMountVelus(), "{5}{R}{R}");
        harness.passBothPriorities();
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new SavannahLions());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    private void castTerrorOfMountVelus() {
        harness.castFromHand(player1, new TerrorOfMountVelus(), "{5}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
