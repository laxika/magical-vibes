package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VampireOpportunistTest extends BaseCardTest {

    @Test
    void activationMakesEachOpponentLoseLifeAndControllerGainLife() {
        Permanent opportunist = addReadyOpportunist(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(opportunist.isTapped()).isTrue();
    }

    private Permanent addReadyOpportunist(Player player) {
        Permanent opportunist = new Permanent(new VampireOpportunist());
        opportunist.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(opportunist);
        return opportunist;
    }
}
