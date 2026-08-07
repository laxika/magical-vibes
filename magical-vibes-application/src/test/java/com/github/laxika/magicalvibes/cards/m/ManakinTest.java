package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManakinTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds {C}")
    void tapAddsColorless() {
        harness.addToBattlefield(player1, new Manakin());
        GameData gd = harness.getGameData();
        Permanent manakin = gd.playerBattlefields.get(player1.getId()).getFirst();
        manakin.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(manakin.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty(); // mana ability resolves immediately
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new Manakin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }
}
