package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VintaraElephantTest extends BaseCardTest {

    @Test
    void anyPlayerMayPayToRemoveTrample() {
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new VintaraElephant());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void lostTrampleReturnsAtEndOfTurn() {
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new VintaraElephant());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isTrue();
    }
}
