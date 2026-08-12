package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KabiraCrossroadsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 2 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new KabiraCrossroads()));

        harness.playLand(player1, 0);

        Permanent crossroads = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(crossroads.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Tapping for mana adds white mana")
    void tapsForWhiteMana() {
        Permanent crossroads = new Permanent(new KabiraCrossroads());
        crossroads.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(crossroads);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(crossroads.isTapped()).isTrue();
    }
}
