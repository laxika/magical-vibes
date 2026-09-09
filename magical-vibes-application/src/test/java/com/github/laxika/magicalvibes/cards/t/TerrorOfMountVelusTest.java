package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerrorOfMountVelus.class, GrizzlyBears.class})
class TerrorOfMountVelusTest extends BaseCardTest {

    @Test
    void enteringGrantsDoubleStrikeToOwnCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTerrorOfMountVelus();

        Permanent terror = findPermanent(player1, "Terror of Mount Velus");
        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(terror.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(opponentBears.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void grantedDoubleStrikeExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castTerrorOfMountVelus();

        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void castTerrorOfMountVelus() {
        harness.setHand(player1, List.of(new TerrorOfMountVelus()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
