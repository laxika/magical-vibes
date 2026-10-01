package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SimicRagworm.class})
class SimicRagwormTest extends BaseCardTest {

    @Test
    void payingBlueManaUntapsSimicRagworm() {
        Permanent ragworm = addCreatureReady(player1, new SimicRagworm());
        ragworm.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ragworm.isTapped()).isFalse();
    }

    @Test
    void activatingAbilityDoesNotTapSimicRagworm() {
        Permanent ragworm = addCreatureReady(player1, new SimicRagworm());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(ragworm.isTapped()).isFalse();
    }

    @Test
    void activatingAbilityRequiresBlueMana() {
        Permanent ragworm = addCreatureReady(player1, new SimicRagworm());
        ragworm.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ragworm.isTapped()).isTrue();
    }
}
