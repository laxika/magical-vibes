package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MoxRuby.class)
class MoxRubyTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mox Ruby adds one red mana")
    void tappingAddsRedMana() {
        Permanent mox = harness.addToBattlefieldAndReturn(player1, new MoxRuby());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(mox.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
