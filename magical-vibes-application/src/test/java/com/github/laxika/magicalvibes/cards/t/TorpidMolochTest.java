package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorpidMoloch.class, Mountain.class})
class TorpidMolochTest extends BaseCardTest {

    @Test
    void sacrificesThreeLandsAndLosesDefenderUntilEndOfTurn() {
        Permanent moloch = addCreatureReady(player1, new TorpidMoloch());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(moloch.hasKeyword(Keyword.DEFENDER)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(moloch.hasKeyword(Keyword.DEFENDER)).isTrue();
    }

    @Test
    void cannotActivateWithoutThreeLands() {
        addCreatureReady(player1, new TorpidMoloch());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
