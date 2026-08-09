package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VampireNeonateTest extends BaseCardTest {

    @Test
    void activationMakesEachOpponentLoseLifeAndControllerGainLife() {
        Permanent neonate = addReadyNeonate(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(neonate.isTapped()).isTrue();
    }

    private Permanent addReadyNeonate(Player player) {
        Permanent neonate = new Permanent(new VampireNeonate());
        neonate.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(neonate);
        return neonate;
    }
}
