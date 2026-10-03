package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BullCerodon.class})
class BullCerodonTest extends BaseCardTest {

    @Test
    @DisplayName("Haste — attacks and deals damage the turn it enters while summoning sick")
    void hasteAllowsAttackWhileSummoningSick() {
        Permanent cerodon = harness.addToBattlefieldAndReturn(player1, new BullCerodon());
        assertThat(cerodon.isSummoningSick()).isTrue();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("Vigilance — attacking does not tap it")
    void vigilanceKeepsUntappedWhenAttacking() {
        Permanent cerodon = addCreatureReady(player1, new BullCerodon());

        declareAttackers(player1, List.of(0));

        assertThat(cerodon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Haste and vigilance allow an untapped attack on the turn it enters")
    void attacksWithoutTappingWhileSummoningSick() {
        Permanent cerodon = harness.enterBattlefieldAndReturn(player1, new BullCerodon());
        assertThat(cerodon.isSummoningSick()).isTrue();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(cerodon.isAttacking()).isTrue();
        assertThat(cerodon.isTapped()).isFalse();
    }
}
