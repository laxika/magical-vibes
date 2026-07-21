package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SunscorchedDesert;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuneDivinerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} and tapping a Desert gains 1 life")
    void activateGainsLifeAndTapsDesert() {
        Permanent diviner = harness.addToBattlefieldAndReturn(player1, new DuneDiviner());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new SunscorchedDesert());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(diviner);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(desert.isTapped()).isTrue();
        assertThat(diviner.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without an untapped Desert")
    void cannotActivateWithoutUntappedDesert() {
        Permanent diviner = harness.addToBattlefieldAndReturn(player1, new DuneDiviner());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new SunscorchedDesert());
        desert.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(diviner);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a Desert")
    void cannotActivateWithoutDesert() {
        Permanent diviner = harness.addToBattlefieldAndReturn(player1, new DuneDiviner());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(diviner);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
