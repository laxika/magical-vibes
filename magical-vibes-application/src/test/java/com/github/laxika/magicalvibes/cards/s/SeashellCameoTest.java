package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeashellCameoTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Seashell Cameo adds chosen white or blue mana")
    void tappingAddsChosenMana() {
        Permanent cameo = new Permanent(new SeashellCameo());
        cameo.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(cameo);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(cameo.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
