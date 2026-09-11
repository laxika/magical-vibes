package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TalruumMinotaur.class)
class TalruumMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows Talruum Minotaur to attack immediately after entering")
    void hasteAllowsImmediateAttack() {
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new TalruumMinotaur());
        assertThat(minotaur.isSummoningSick()).isTrue();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }
}
